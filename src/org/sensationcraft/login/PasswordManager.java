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

public class PasswordManager
{

    private SCLogin plugin;
    private Database olddb;
    private PreparedStatement getid;
    private PreparedStatement delid;

    protected PasswordManager(SCLogin plugin)
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

    // Only call this in an async task to prevent lag
    public boolean checkPassword(String player, String checkPass)
    {
        String realPass = "";
        PasswordType type = PasswordType.DEFAULT;
        int id = -1;
        if (olddb != null && this.getid != null)
        {
            ResultSet result = null;
            try
            {
                this.getid.setString(1, player);
                result = this.getid.executeQuery();
                if (!result.next())
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
            // update hash in database to use xAuth's hashing method
            String newHash = PasswordHandler.hash(checkPass);

            try
            {
                this.delid.setInt(1, id);
                this.delid.executeUpdate();
            }
            catch (SQLException e)
            {
                //xAuthLog.severe("Failed to update password hash for account: " + accountId, e);
            }
            finally
            {
                //plugin.getDbCtrl().close(conn, ps);
            }

            return true;
        }
        else
        {
            return false;
        }
    }
}
