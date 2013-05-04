package org.sensationcraft.login;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.bukkit.plugin.java.JavaPlugin;
import org.sensationcraft.login.sql.Database;
import org.sensationcraft.login.sql.SQLite;
import org.sensationcraft.login.sql.TableBuilder;

public class SCLogin extends JavaPlugin{
    
        Database database;
        
        PlayerManager playermngr;
        
        PasswordManager passwordmngr;
    
	@Override
	public void onEnable(){
		this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new AuthenticationListener(this), this);
                
                getDataFolder().mkdirs();
                File db = new File(getDataFolder(), "SClogin.db");
                database = new SQLite(getLogger(), db);
                playermngr = new PlayerManager(this);
                passwordmngr = new PasswordManager();
                if(!initSQL())
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
        
        /**
         * Getter for the Database object used for SQL queries
         * @return the Database object
         */
        public Database getConnection()
        {
            return this.database;
        }
        
        /**
         * Getter for the PlayerManager object used for player related storage
         * @return the PlayerManager object
         */
        public PlayerManager getPlayerManager()
        {
            return this.playermngr;
        }

        /**
         * Getter for the PasswordManager object used for password related storage
         * @return the PasswordManager object
         */
        public PasswordManager getPasswordManager()
        {
            return this.passwordmngr;
        }
        
}
