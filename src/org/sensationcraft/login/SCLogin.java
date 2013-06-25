package org.sensationcraft.login;

import java.io.BufferedOutputStream;
import org.sensationcraft.login.listeners.AuthenticationListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.sensationcraft.login.commands.ChangePasswordCommand;
import org.sensationcraft.login.commands.LoginCommand;
import org.sensationcraft.login.commands.LogoutCommand;
import org.sensationcraft.login.commands.QuitCommand;
import org.sensationcraft.login.commands.RegisterCommand;
import org.sensationcraft.login.commands.SCLoginCommand;
import org.sensationcraft.login.commands.SCLoginMasterCommand;
import org.sensationcraft.login.commands.UnregisterCommand;
import org.sensationcraft.login.listeners.InventoryListener;
import org.sensationcraft.login.listeners.PlayerListener;
import org.sensationcraft.login.sql.Database;
import org.sensationcraft.login.sql.SQLite;
import org.sensationcraft.login.sql.TableBuilder;
import org.sensationcraft.login.strikes.StrikeManager;

public class SCLogin extends JavaPlugin{

	private Database database;

	private PlayerManager playermngr;

	private PasswordManager passwordmngr;
        
        private StrikeManager strikemngr;
        
        private xAuthHook hook;

	private Map<String, SCLoginMasterCommand> commands = new HashMap<String, SCLoginMasterCommand>();
        
        public static final boolean debug = false;
        
        private static SCLogin instance;
        
        private Logger log;
        
        public static boolean timings = true;
        
	@Override
	public void onEnable()
        {
                instance = this;
                
                if(timings)
                {
                    File logFile = new File(getDataFolder(), "timings.log");
                    if(!logFile.exists())
                    {
                        try
                        {
                            logFile.getParentFile().mkdirs();
                            if(!logFile.createNewFile())
                            {
                                throw new IOException("Failed to create log file");
                            }
                        }
                        catch(IOException ex)
                        {
                            logFile = null;
                        }
                    }

                    if(logFile != null)
                    {
                        this.log = Logger.getLogger("SCLogin_timings");
                        for(Handler h : this.log.getHandlers())
                            this.log.removeHandler(h);
                        try
                        {
                            FileHandler fh = new FileHandler();
                            OutputStream out = new FileOutputStream(logFile, true);
                            Method m = java.util.logging.StreamHandler.class.getDeclaredMethod("setOutputStream", OutputStream.class);
                            if(!m.isAccessible()) m.setAccessible(true);
                            m.invoke(fh, out);
                            fh.setFormatter(new Formatter() {

                                @Override
                                public String format(LogRecord record)
                                {
                                    return record.getMessage()+"\n";
                                }
                            });
                            this.log.addHandler(fh);
                        }
                        catch(Exception ex)
                        {
                            getLogger().warning("Failed to initialize the timings logger");
                            ex.printStackTrace();
                        }
                    }
                }
                
		this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new AuthenticationListener(this), this);
		this.getLogger().info("Initializing commands...");
		this.getDataFolder().mkdirs();
		File db = new File(this.getDataFolder(), "SClogin.db");
		this.database = new SQLite(this.getLogger(), db);
		if(!this.initSQL())
		{
                    getLogger().log(Level.SEVERE, "Could not connect to database!");
                    Bukkit.getPluginManager().disablePlugin(this);
                    return;
		}
                this.hook = new xAuthHook(this);
                this.playermngr = new PlayerManager(this);
		this.passwordmngr = new PasswordManager(this);
                this.strikemngr = new StrikeManager(this);
                Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
                Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
                this.initCommandMap();
                for(Player player : Bukkit.getOnlinePlayers())
                {
                    player.sendMessage(ChatColor.GREEN+"Server reloaded, you have been automagically logged out.");
                }
	}

        @Override
        public void onDisable()
        {
            if(this.hook != null && this.hook.isHooked())
            {
                this.hook.unhook();
            }
            
            if(this.database != null)
                this.database.close();
            
            instance = null;
        }
	
        public static SCLogin getInstance()
        {
            return instance;
        }
        
	@Override
	public boolean onCommand(CommandSender sender, Command command, String name, String[] args){
		SCLoginMasterCommand scLoginCommand = this.commands.get(command.getName().toLowerCase());
		return scLoginCommand == null ? false:scLoginCommand.execute(sender, args);
	}
        
        /**
	 * Initializes the SQL connection and the table structure
	 * @return whether the connection was established and if
	 * the tables were either found or created with success
	 */
	private boolean initSQL()
	{
		if(!this.database.connect()) return false;

                getLogger().info("Connection was successfull");
                
		if(!this.database.checkTable("players"))
		{
			TableBuilder players = new TableBuilder("players");
			players.addColumn("id", "INT");
			players.addColumn("username", "varchar(16)").addProperty("UNIQUE").addProperty("NOT NULL");
			players.addColumn("password", "varchar(100)").addProperty("NOT NULL");
			players.addColumn("lastip", "varchar(16)").addProperty("NOT NULL");
			players.addColumn("email", "varchar(50)").addProperty("DEFAULT ''");
                        players.addColumn("locked", "TINYINT").addProperty("DEFAULT 0");
                        players.setPrimaryKey("id");
			players.createTable(this.database);
		}
                
                if(!this.database.checkTable("lockouts"))
                {
                    TableBuilder lockouts = new TableBuilder("lockouts");
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
		LoginCommand login = new LoginCommand(this);
		this.commands.put("login", login);
		this.commands.put("l", login);
                
                ChangePasswordCommand cpw = new ChangePasswordCommand(this);
                this.commands.put("changepassword", cpw);
                this.commands.put("changepw", cpw);
                this.commands.put("cpw", cpw);
		this.commands.put("register", new RegisterCommand(this));
                this.commands.put("unregister", new UnregisterCommand(this));
		this.commands.put("sclogin", new SCLoginCommand(this));
                this.commands.put("logout", new LogoutCommand(this));
                
                QuitCommand quit = new QuitCommand(this);
                this.commands.put("quit", quit);
                this.commands.put("q", quit);
	}

}
