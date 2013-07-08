package org.sensationcraft.login;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.sensationcraft.login.commands.ChangePasswordCommand;
import org.sensationcraft.login.commands.LoginCommand;
import org.sensationcraft.login.commands.QuitCommand;
import org.sensationcraft.login.commands.RegisterCommand;
import org.sensationcraft.login.commands.SCLoginCommand;
import org.sensationcraft.login.commands.SCLoginMasterCommand;
import org.sensationcraft.login.commands.SafeguardCommand;
import org.sensationcraft.login.commands.UnregisterCommand;
import org.sensationcraft.login.listeners.AuthenticationListener;
import org.sensationcraft.login.listeners.ChatPacketListener;
import org.sensationcraft.login.listeners.InventoryListener;
import org.sensationcraft.login.listeners.PlayerListener;
import org.sensationcraft.login.messages.Messages;
import org.sensationcraft.login.sql.Database;
import org.sensationcraft.login.sql.SQLite;
import org.sensationcraft.login.sql.TableBuilder;
import org.sensationcraft.login.strikes.StrikeManager;

import com.comphenix.protocol.ProtocolLibrary;

public class SCLogin extends JavaPlugin
{

	private Database database;
	private PlayerManager playermngr;
	private PasswordManager passwordmngr;
	private StrikeManager strikemngr;
	private xAuthHook hook;
	private final Map<String, SCLoginMasterCommand> commands = new HashMap<String, SCLoginMasterCommand>();
	public static final boolean debug = false;
	private static SCLogin instance;
	public static boolean timings = true;

	@Override
	public void onEnable()
	{
		SCLogin.instance = this;

		this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new AuthenticationListener(this), this);
		Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
		Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
		ProtocolLibrary.getProtocolManager().addPacketListener(new ChatPacketListener(this));
		this.getLogger().info("Initializing hook");
		this.hook = new xAuthHook(this);
		this.getLogger().info("Initializing SQLite connection");
		this.getDataFolder().mkdirs();
		final File db = new File(this.getDataFolder(), "SClogin.db");
		this.database = new SQLite(this.getLogger(), db);
		if (!this.initSQL())
		{
			this.getLogger().log(Level.SEVERE, "Could not connect to database!");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		this.getLogger().info("Initializing managers");
		this.playermngr = new PlayerManager(this);
		this.passwordmngr = new PasswordManager(this);
		this.strikemngr = new StrikeManager(this);
		this.getLogger().info("Initializing commands...");
		this.initCommandMap();
		for (final Player player : Bukkit.getOnlinePlayers())
			player.sendMessage(Messages.RELOAD_LOGOUT.getMessage());
	}

	@Override
	public void onDisable()
	{
		if (this.hook != null && this.hook.isHooked())
			this.hook.unhook();

		if (this.database != null)
			this.database.close();

		SCLogin.instance = null;
	}

	public static SCLogin getInstance()
	{
		return SCLogin.instance;
	}

	@Override
	public boolean onCommand(final CommandSender sender, final Command command, final String name, final String[] args)
	{
		final SCLoginMasterCommand scLoginCommand = this.commands.get(command.getName().toLowerCase());
		return scLoginCommand == null ? false : scLoginCommand.execute(sender, args);
	}

	/**
	 * Initializes the SQL connection and the table structure
	 *
	 * @return whether the connection was established and if the tables were
	 * either found or created with success
	 */
	private boolean initSQL()
	{
		if (!this.database.connect())
			return false;

		this.getLogger().info("Connection was successfull");

		if (!this.database.checkTable("players"))
		{
			final TableBuilder players = new TableBuilder("players");
			players.addColumn("id", "INT");
			players.addColumn("username", "varchar(16)").addProperty("UNIQUE").addProperty("NOT NULL");
			players.addColumn("password", "varchar(100)").addProperty("NOT NULL");
			players.addColumn("lastip", "varchar(16)").addProperty("NOT NULL");
			players.addColumn("email", "varchar(50)").addProperty("DEFAULT ''");
			players.addColumn("locked", "TINYINT").addProperty("NOT NULL ").addProperty("DEFAULT 0");
			players.setPrimaryKey("id");
			players.createTable(this.database);
		}

		if (!this.database.checkTable("lockouts"))
		{
			final TableBuilder lockouts = new TableBuilder("lockouts");
			lockouts.addColumn("ip", "VARCHAR(16)").addProperty("UNIQUE").addProperty("NOT NULL");
			lockouts.addColumn("till", "TIMESTAMP").addProperty("NOT NULL");
			lockouts.createTable(this.database);
		}
		return this.database.checkTable("players") && this.database.checkTable("lockouts");
	}

	public Database getConnection()
	{
		return this.database;
	}

	public PlayerManager getPlayerManager()
	{
		return this.playermngr;
	}

	public PasswordManager getPasswordManager()
	{
		return this.passwordmngr;
	}

	public StrikeManager getStrikeManager()
	{
		return this.strikemngr;
	}

	public xAuthHook getxAuthHook()
	{
		return this.hook;
	}

	private void initCommandMap()
	{
		final LoginCommand login = new LoginCommand(this);
		this.commands.put("login", login);
		this.commands.put("l", login);

		final ChangePasswordCommand cpw = new ChangePasswordCommand(this);
		this.commands.put("changepassword", cpw);
		this.commands.put("changepw", cpw);
		this.commands.put("cpw", cpw);
		this.commands.put("register", new RegisterCommand(this));
		this.commands.put("unregister", new UnregisterCommand(this));
		this.commands.put("sclogin", new SCLoginCommand(this));
		//this.commands.put("logout", new LogoutCommand(this));
		this.commands.put("safeguard", new SafeguardCommand(this));

		final QuitCommand quit = new QuitCommand();
		this.commands.put("quit", quit);
		this.commands.put("q", quit);
	}
}
