package org.sensationcraft.login;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import org.sensationcraft.login.password.PasswordHandler;
import org.sensationcraft.login.password.PasswordType;
import org.sensationcraft.login.sql.Database;
import org.sensationcraft.login.sql.H2;


public class xAuthHook 
{
    private SCLogin plugin;
    
    private Database olddb;
    
    private PreparedStatement getdata;
    private final Object getdataLock = new Object();
    
    private PreparedStatement delid;
    private final Object delid_lock = new Object();
    
    
    protected xAuthHook(SCLogin plugin)
    {
        this.plugin = plugin;
        File[] toDelete = new File[]{new File(plugin.getDataFolder(), "xAuth.h2.db"), new File(plugin.getDataFolder(), "xAuth.lock.db")};
        if(!toDelete[0].exists()) return;
        olddb = new H2(plugin.getLogger(), this.plugin.getDataFolder(), "xAuth", "sa");
        if (olddb.connect())
        {
            ResultSet result = null;
            try
            {
                result = olddb.executeQuery("SELECT * FROM accounts");
                if (!result.next())
                {
                    plugin.getLogger().log(Level.INFO, "xAuth database seems to be completely copied, deleting old files...");
                    olddb.close();
                    for(File f : toDelete)
                    {
                        if(f.exists())
                        {
                            f.delete();
                        }
                    }
                }
            }
            catch (SQLException ex)
            {
                plugin.getLogger().log(Level.WARNING, "An error occurred while counting the leftover players in the xAuth database: {0}", ex.getMessage());
            }
            finally
            {
                if (result != null)
                {
                    try
                    {
                        result.close();
                    }
                    catch (SQLException ex)
                    {
                    }
                }
            }
        }

        if (olddb.isReady())
        {
            this.getdata = olddb.prepare("SELECT `id`,`password`, `pwtype` FROM `accounts` WHERE LOWER(`playername`) = LOWER(?)");
            this.delid = olddb.prepare("DELETE FROM `accounts` WHERE id = ?");
            plugin.getLogger().log(Level.INFO, "Hooked into the old xAuth database.");
        }
    }
    
    public boolean isHooked()
    {
        return olddb != null && this.getdata != null;
    }
    
    public void unhook()
    {
        if(this.delid != null)
        try
        {
            this.delid.close();
        }catch(SQLException ex){}
        
        if(this.getdata != null)
        try
        {
            this.getdata.close();
        }catch(SQLException ex){}
        
        if(this.olddb != null)
            this.olddb.close();        
    }
    
    public void checkPassword_old(PasswordManager.PlayerCheck reference, String checkPass)
    {
        String player = reference.getName();
        String realPass = "";
        PasswordType type = PasswordType.DEFAULT;
        ResultSet result = null;
        int id = Integer.MIN_VALUE;
        try
        {
            result = Database.synchronizedExecuteQuery(getdata, getdataLock, player);
            if (result == null || !result.next())
            {
                throw new SQLException("Player not found you say?");
            }
            id = result.getInt("id");
            realPass = result.getString("password");
            type = PasswordType.getType(result.getInt("pwtype"));
        }
        catch (SQLException ex)
        {
            System.out.println("Nothing found, please continue");
            return;
        }
        finally
        {
            if (result != null)
            {
                try
                {
                    result.close();
                }
                catch (SQLException ex)
                {
                }
            }
        }
        String checkPassHash = "";
        if (type == PasswordType.DEFAULT)
        {
            int saltPos = (checkPass.length() >= realPass.length() ? realPass.length() - 1 : checkPass.length());
            String salt = realPass.substring(saltPos, saltPos + 12);
            String hash = PasswordHandler.whirlpool(salt + checkPass);
            checkPassHash = hash.substring(0, saltPos) + salt + hash.substring(saltPos);
        }
        else if (type == PasswordType.WHIRLPOOL)
        {
            checkPassHash = PasswordHandler.whirlpool(checkPass);
        }
        else if (type == PasswordType.AUTHME_SHA256)
        {
            String salt = realPass.split("\\$")[2];
            checkPassHash = "$SHA$" + salt + "$" + PasswordHandler.hash(PasswordHandler.hash(checkPass, "SHA-256") + salt, "SHA-256");
        }
        else
        {
            checkPassHash = PasswordHandler.hash(checkPass, type.getAlgorithm());
        }

        this.plugin.getLogger().log(Level.INFO, "{0}:{1}",new Object[]{checkPassHash, realPass});
        
        if (checkPassHash.equals(realPass))
        {
            reference.authenticate();
            // update hash in database to use xAuth's hashing method
            Database.synchronizedExecuteUpdate(delid, delid_lock, id);
        }
    }
    
    public boolean isRegistered(String name)
    {
        try
        {
            ResultSet result  = Database.synchronizedExecuteQuery(getdata, getdataLock, name);
            return (result != null && result.next());
        }
        catch(SQLException ex)
        {
            
        }
        return false;
    }
}
