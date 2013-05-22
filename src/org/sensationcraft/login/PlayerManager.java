package org.sensationcraft.login;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.sensationcraft.login.sql.Database;

public class PlayerManager
{

	enum Status
	{
		NOT_LOGGED_IN, AUTHENTICATED;
	}

	private Map<String, Status> playerStatus = new HashMap<String, Status>();
	private final Object statusLock = new Object();

	private SCLogin plugin;

	private PreparedStatement registered;
	private final Object registeredLock = new Object();
        
        private PreparedStatement register;
        private final Object registerLock = new Object();

	private PreparedStatement ip;
	private final Object ipLock = new Object();

	private PreparedStatement email;
	private final Object emailLock = new Object();

	protected PlayerManager(SCLogin plugin)
	{
		this.plugin = plugin;
		this.registered = this.plugin.getConnection().prepare("SELECT * FROM `players` WHERE `username` = ?");
		this.ip = this.plugin.getConnection().prepare("SELECT `lastip` FROM `players` WHERE `username` = ?");
		this.register = this.plugin.getConnection().prepare("INSERT INTO `players`(`username`, `password`, `lastip`, `email`) VALUES(?, ?, ?, ?)");
                this.email = this.plugin.getConnection().prepare("SELECT `email` FROM `players` WHERE `username` = ?");
	}

	public boolean isRegistered(String name)
	{
            xAuthHook hook = this.plugin.getxAuthHook();
            ResultSet result = null;
            try
            {
                    this.registered.setString(1, name);
                    result = Database.synchronizedExecuteQuery(this.registered, this.registeredLock, name);
                    return (result != null && result.next()) || (hook.isHooked() && hook.isRegistered(name));
            }
            catch(SQLException ex)
            {
                    // Might log this
            }
            finally
            {
                    if(result != null)
                    {
                            try
                            {
                                    result.close();
                            }catch(SQLException ex){}
                    }
            }
            return false;
	}

	public String getLastIp(String name)
	{
                ResultSet result = null;
                try
                {
                        result = Database.synchronizedExecuteQuery(this.ip, this.ipLock, name);
                        if(!result.next()) return "";
                        return result.getString("lastip");
                }
                catch(SQLException ex)
                {
                    
                }
                finally
                {
                        if(result != null)
                        {
                                try
                                {
                                        result.close();
                                }catch(SQLException ex){}
                        }
                }
                return "";
	}

	public String getEmail(String name)
	{
                ResultSet result = null;
                try
                {
                        result = Database.synchronizedExecuteQuery(email, emailLock, name);
                        if(!result.next()) return "";
                        return result.getString("email");
                }
                catch(SQLException ex)
                {
                        // Might log this
                }
                finally
                {
                        if(result != null)
                        {
                                try
                                {
                                        result.close();
                                }catch(SQLException ex){}
                        }
                }
                return "";
	}

	public boolean isLoggedIn(String name)
	{
		synchronized(this.statusLock)
		{
			return this.playerStatus.get(name) == Status.AUTHENTICATED;
		}
	}

	public boolean isOnline(String name)
	{
		synchronized(this.statusLock)
		{
			return this.playerStatus.get(name) != null;
		}
	}

	public void join(String name)
	{
		synchronized(this.statusLock)
		{
			this.playerStatus.put(name, Status.NOT_LOGGED_IN);
		}
	}

	public void quit(String name)
	{
		synchronized(this.statusLock)
		{
			this.playerStatus.remove(name);
		}
	}
	public void doLogin(String name)
        {
		synchronized(this.statusLock)
                {
			this.playerStatus.put(name, Status.AUTHENTICATED);
			Player player = this.plugin.getServer().getPlayerExact(name);
			if(player == null)
                        {
				this.quit(name);
				return;
			}
			player.sendMessage(ChatColor.GREEN+"You have been logged in!");
		}
	}
        
	public boolean register(String name, String pass, String ip) throws Exception
        {
            if(this.isRegistered(name))
            {
                return false;
            }
            
            Database.synchronizedExecuteUpdate(this.register, this.registerLock, name, pass, ip);
            
            return this.isRegistered(name);
	}

}
