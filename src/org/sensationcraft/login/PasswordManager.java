package org.sensationcraft.login;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.sensationcraft.login.sql.Database;

public class PasswordManager
{

    private SCLogin plugin;
    
    private PreparedStatement getpass;
    private final Object getpassLock = new Object();
    
    private PreparedStatement cpw;
    private final Object cpwLock = new Object();
    
    protected PasswordManager(SCLogin plugin)
    {
        this.plugin = plugin;
        this.getpass = this.plugin.getConnection().prepare("SELECT `password` FROM `players` WHERE `username` = ?");
        this.cpw = this.plugin.getConnection().prepare("UPDATE `players` SET `password` = ? WHERE `username` = ?");
    }

    // Only call this in an async task to prevent lag
    // IP is for the registration
    public boolean checkPassword(String player, String checkPass, String ip)
    {
        if(checkPass == null || checkPass.isEmpty()) return false;
        PlayerCheck reference = new PlayerCheck(player);
        xAuthHook hook = this.plugin.getxAuthHook();
        if (hook.isHooked())
        {
            hook.checkPassword(reference, checkPass);
        }
        
        if(reference.isAuthenticated())
        {
            // Register them because it is highly likely they just joined
            // using this authentication
            try
            {
                this.plugin.getPlayerManager().register(player, checkPass, ip);
                this.plugin.getPlayerManager().setLocked(player, reference.isLocked());
                return !reference.isLocked();
            }
            catch(Exception ex)
            {
                // Swallow the exception, might log it later on
            }
            return true;
        }
        
        // New check
        if(this.getpass != null)
        {
            int i = 0;
            while(i++ < 3)
            {
                ResultSet result = null;
                try
                {
                    result = Database.synchronizedExecuteQuery(getpass, getpassLock, player);
                    if(result == null || !result.next()) return false;
                    try
                    {
                        return checkPass.equals(result.getString("password"));
                    }
                    catch(NullPointerException ex)
                    {

                    }
                }
                catch(SQLException ex)
                {
                    // Log it maybe?
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
            }
        }
        return false;
    }
    
    public boolean changePassword(String name, String password)
    {
        if(this.plugin.getPlayerManager().isRegistered(name))
        {
            return false;
        }
        Database.synchronizedExecuteUpdate(cpw, cpwLock, name, password);
        return true;
    }
    
    // Object for storing data as reference
    protected static class PlayerCheck
    {
        enum Auth
        {
            NOPE,OK
        }
        
        private final String name;
        
        private Auth authenticated;
        
        private boolean isLocked = false;
        
        PlayerCheck(String name)
        {
            this.name = name;
            this.authenticated = Auth.NOPE;
        }
        
        public String getName()
        {
            return this.name;
        }
        
        public void authenticate()
        {
            this.authenticated = Auth.OK;
        }
        
        public void lock()
        {
            this.isLocked = true;
        }
        
        public boolean isAuthenticated()
        {
            return this.authenticated == Auth.OK;
        }
        
        public boolean isLocked()
        {
            return this.isLocked;
        }
    }
}
