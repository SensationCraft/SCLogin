package org.sensationcraft.login;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.IllegalFormatException;
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

        private PreparedStatement unregister;
        private final Object unregisterLock = new Object();
        
	private PreparedStatement ip;
	private final Object ipLock = new Object();

	private PreparedStatement email;
	private final Object emailLock = new Object();
        
        private PreparedStatement isLocked;
        private final Object isLockedLock = new Object();
        
        private PreparedStatement setLock;
        private final Object setLockLock = new Object();
        
        private PreparedStatement getActiveCount;
        private final Object getActiveCountLock = new Object();
        
        private PreparedStatement getLockedCount;
        private final Object getLockedCountLock = new Object();

	protected PlayerManager(SCLogin plugin)
	{
		this.plugin = plugin;
		this.registered = this.plugin.getConnection().prepare("SELECT * FROM `players` WHERE `username` = ?");
		this.ip = this.plugin.getConnection().prepare("SELECT `lastip` FROM `players` WHERE `username` = ?");
		this.register = this.plugin.getConnection().prepare("INSERT INTO `players`(`username`, `password`, `lastip`, `email`, `locked`) VALUES(?, ?, ?, ?, ?)");
                this.email = this.plugin.getConnection().prepare("SELECT `email` FROM `players` WHERE `username` = ?");
                this.unregister = this.plugin.getConnection().prepare("DELETE FROM `players` WHERE `username` = ?");
                this.isLocked = this.plugin.getConnection().prepare("SELECT locked FROM `players` WHERE `username` = ?");
                this.setLock = this.plugin.getConnection().prepare("UPDATE `players` SET `locked` = ? WHERE `username` = ?");
                this.getActiveCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` > 0");
                this.getLockedCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` = 0");
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
            
            Database.synchronizedExecuteUpdate(this.register, this.registerLock, name, pass, ip, 0);
            
            return this.isRegistered(name);
	}
        
        public boolean unregister(String name)
        {
            if(!this.isRegistered(name))
            {
                return false;
            }
            
            Database.synchronizedExecuteUpdate(unregister, unregisterLock, name);
            
            return this.isRegistered(name);
        }
        
        public boolean isLocked(String name)
        {
            ResultSet result = Database.synchronizedExecuteQuery(this.isLocked, this.isLockedLock, name);
            try
            {
                if(result == null || !result.next()) return false;
                return result.getInt("locked") > 0;
            }
            catch(SQLException ex)
            {
                // Swallow the exception
            }
            return false;
        }
        
        public boolean setLocked(String name, boolean flag)
        {
            Database.synchronizedExecuteUpdate(this.setLock, this.setLockLock, (flag ? 1 : 0), name);
            return isLocked(name);
        }
        
        public String getProfile(String name)
        {
            if(!this.isRegistered(name))
            {
                return ChatColor.RED+"This player has not played before.";
            }
            
            StringBuilder profile = new StringBuilder("----- SCLogin profile -----\n");
            profile.append("Name: ").append(name).append("\n");
            boolean isOnline = this.isOnline(name);
            profile.append("Online: ").append(getValueColour(isOnline)).append(isOnline).append(ChatColor.RESET).append("\n");
            boolean isAuthenticated = this.isLoggedIn(name);
            profile.append("Authenticated: ").append(getValueColour(isAuthenticated)).append(isAuthenticated).append(ChatColor.RESET).append("\n");
            profile.append("IP: ").append(this.getLastIp(name)).append("\n");
            profile.append("Last authentication: ").append("not implemented yet").append("\n");
            boolean isLocked = isLocked(name);
            profile.append("Locked: ").append(getValueColour(isLocked)).append(isLocked).append(ChatColor.RESET).append("\n");
            return profile.append("---------------------------").toString();
        }
        
        private ChatColor getValueColour(boolean flag)
        {
            return flag ? ChatColor.GREEN : ChatColor.RED;
        }
        
        public String getCount(String what)
        {
            if(what == null) return null;
            ResultSet result = null;
            if(what.startsWith("a"))
            {
                result = Database.synchronizedExecuteQuery(this.getActiveCount, this.getActiveCountLock);
            }
            else if(what.startsWith("l"))
            {
                result = Database.synchronizedExecuteQuery(this.getLockedCount, this.getLockedCountLock);
            }
            try
            {
                if(result != null && result.next())
                {
                    return String.format("%d", result.getInt("count"));
                }
            }
            catch(SQLException ex)
            {
                // Swallow the exception
            }
            catch(IllegalFormatException ex)
            {
                // Swallow the exception
            }
            return null;
        }

}
