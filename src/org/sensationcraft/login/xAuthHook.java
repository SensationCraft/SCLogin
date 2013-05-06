package org.sensationcraft.login;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import org.sensationcraft.login.password.PasswordHandler;
import org.sensationcraft.login.password.PasswordType;
import org.sensationcraft.login.sql.Database;
import org.sensationcraft.login.sql.SQLite;


public class xAuthHook 
{
    private SCLogin plugin;
    
    private Database olddb;
    
    private PreparedStatement getid;
    private final Object getid_lock = new Object();
    
    private PreparedStatement delid;
    private final Object delid_lock = new Object();
    
    protected xAuthHook(SCLogin plugin)
    {
        this.plugin = plugin;
        File oldfile = new File(plugin.getDataFolder(), "xauth.db");
        if (oldfile.exists())
        {
            olddb = new SQLite(plugin.getLogger(), oldfile);
            if (olddb.connect())
            {
                ResultSet result = null;
                try
                {
                    result = olddb.executeQuery("SELECT * FROM accounts");
                    if (!result.next())
                    {
                        olddb.close();
                        oldfile.delete();
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
                this.getid = olddb.prepare("SELECT `id` FROM `accounts` WHERE `playername` = ?");
                this.delid = olddb.prepare("DELETE FROM TABLE `accounts` WHERE id = ?");
            }
        }
    }
    
    public boolean isHooked()
    {
        return olddb != null && this.getid != null;
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
            result = Database.synchronizedExecuteQuery(getid, getid_lock, player);
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
            ResultSet result  = Database.synchronizedExecuteQuery(getid, getid_lock, name);
            return (result != null && result.next());
        }
        catch(SQLException ex)
        {
            
        }
        return false;
    }
}
