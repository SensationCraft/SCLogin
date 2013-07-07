package org.sensationcraft.login;

import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;
import org.sensationcraft.login.sql.Database;

public class PasswordManager
{

    private final SCLogin plugin;
    private final PreparedStatement getpass;
    private final Object getpassLock = new Object();
    private final PreparedStatement cpw;
    private final Object cpwLock = new Object();

    protected PasswordManager(final SCLogin plugin)
    {
        this.plugin = plugin;
        this.getpass = this.plugin.getConnection().prepare("SELECT `password` FROM `players` WHERE `username` = ?");
        this.cpw = this.plugin.getConnection().prepare("UPDATE `players` SET `password` = ? WHERE `username` = ?");
    }

    // Only call this in an async task to prevent lag
    // IP is for the registration
    public boolean checkPassword(final String player, final String checkPass, final String ip)
    {
        if (checkPass == null || checkPass.isEmpty())
        {
            return false;
        }
        final PlayerCheck reference = new PlayerCheck(player.toLowerCase());
        final xAuthHook hook = this.plugin.getxAuthHook();
        if (hook.isHooked())
        {
            hook.checkPassword(reference, checkPass);
        }

        if (reference.isAuthenticated())
        {
            // Register them because it is highly likely they just joined
            // using this authentication
            try
            {
                this.plugin.getPlayerManager().register(player, checkPass, ip);
                this.plugin.getPlayerManager().setLocked(player, reference.isLocked());
                return !reference.isLocked();
            }
            catch (final Exception ex)
            {
                // Swallow the exception, might log it later on
            }
            return true;
        }

        boolean r = false;
        // New check
        if (this.getpass != null)
        {
                Map<String, Object> results = new HashMap<String, Object>();
                results.put("password", null);
                if(!Database.synchronizedExecuteQuery(results, this.getpass, this.getpassLock, player.toLowerCase()))
                {
                    return false;
                }

                return checkPass.equals((String)results.get("password"));
        }
        return r;
    }

    public boolean changePassword(final String name, final String password)
    {
        if (this.plugin.getPlayerManager().isRegistered(name))
        {
            return false;
        }
        Database.synchronizedExecuteUpdate(this.cpw, this.cpwLock, password, name);
        return true;
    }

    // Object for storing data as reference
    protected static class PlayerCheck
    {

        enum Auth
        {

            NOPE, OK
        }
        private final String name;
        private Auth authenticated;
        private boolean isLocked = false;

        PlayerCheck(final String name)
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
