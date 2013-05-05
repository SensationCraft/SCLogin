package org.sensationcraft.login;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.sensationcraft.login.commands.LoginCommand;
import org.sensationcraft.login.commands.RegisterCommand;
import org.sensationcraft.login.commands.SCLoginCommand;
import org.sensationcraft.login.commands.SCLoginMasterCommand;
import org.sensationcraft.login.sql.Database;
import org.sensationcraft.login.sql.SQLite;
import org.sensationcraft.login.sql.TableBuilder;

public class SCLogin extends JavaPlugin{

	private Database database;

	private PlayerManager playermngr;

	private PasswordManager passwordmngr;

	private Map<String, SCLoginMasterCommand> commands = new HashMap<String, SCLoginMasterCommand>();

	@Override
	public void onEnable(){
		this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new AuthenticationListener(this), this);
		this.getLogger().info("Initializing commands...");
		this.initCommandMap();

		this.getDataFolder().mkdirs();
		File db = new File(this.getDataFolder(), "SClogin.db");
		this.database = new SQLite(this.getLogger(), db);
		this.playermngr = new PlayerManager(this);
		this.passwordmngr = new PasswordManager(this);
		if(!this.initSQL())
		{

		}
		/*
                TableBuilder tb = new TableBuilder("test");
                tb.addColumn("val1", "varchar(16)").addProperty("NOT NULL");
                sqlite.connect();
                if(!sqlite.checkTable("test"))
                {
                    tb.createTable(sqlite);
                }
                sqlite.executeQuery("INSERT INTO `test`(`val1`) VALUES('hello world')");
                ResultSet result = sqlite.executeQuery("SELECT * FROM `test`");
                try
                {
                    while(result.next())
                    {
                        System.out.println("Found: "+result.getString("val1"));
                    }
                }
                catch(SQLException ex)
                {

                }*/

	}

	/**
	 * Initializes the SQL connection and the table structure
	 * @return whether the connection was established and if
	 * the tables were either found or created with success
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args){
		SCLoginMasterCommand scLoginCommand = this.commands.get(command.getName().toLowerCase());
		return scLoginCommand == null ? false:scLoginCommand.execute(sender, args);
	}
	private boolean initSQL()
	{
		if(!this.database.connect()) return false;

		if(!this.database.checkTable("players"))
		{
			TableBuilder players = new TableBuilder("players");
			players.addColumn("id", "INT").addProperty("NOT NULL").addProperty("AUTO_INCREMENT");
			players.addColumn("username", "varchar(16)").addProperty("UNIQUE").addProperty("NOT NULL");
			players.addColumn("password", "varchar(100)").addProperty("NOT NULL");
			players.addColumn("lastip", "varchar(16)").addProperty("NOT NULL");
			players.addColumn("email", "varchar(50)").addProperty("NOT NULL").addProperty("DEFAULT ''");
			players.createTable(this.database);
		}

		return this.database.checkTable("players");
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
	private void initCommandMap(){
		LoginCommand login = new LoginCommand(this);
		this.commands.put("login", login);
		this.commands.put("l", login);
		this.commands.put("register", new RegisterCommand(this));
		this.commands.put("sclogin", new SCLoginCommand());
	}

}
