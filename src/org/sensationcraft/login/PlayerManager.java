package org.sensationcraft.login;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author DarkSeraphim
 */
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
    
    private PreparedStatement ip;
    private final Object ipLock = new Object();
    
    private PreparedStatement email;
    private final Object emailLock = new Object();
    
    protected PlayerManager(SCLogin plugin)
    {
        this.plugin = plugin;
        this.registered = this.plugin.getConnection().prepare("SELECT * FROM `players` WHERE username = ?");
        this.ip = this.plugin.getConnection().prepare("SELECT `lastip` FROM `players` WHERE username = ?");
    }
    
    public boolean isRegistered(String name)
    {
        synchronized(this.registeredLock)
        {
            ResultSet result = null;
            try
            {
                this.registered.setString(1, name);
                result = this.registered.executeQuery();
                return result.next();
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
    }
    
    public String getLastIp(String name)
    {
        synchronized(this.ipLock)
        {
            ResultSet result = null;
            try
            {
                this.registered.setString(1, name);
                result = this.registered.executeQuery();
                if(!result.next()) return "";
                return result.getString("lastip");
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
    }
    
    public String getEmail(String name)
    {
        synchronized(this.emailLock)
        {
            ResultSet result = null;
            try
            {
                this.email.setString(1, name);
                result = this.registered.executeQuery();
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

}
