package org.sensationcraft.login.api;

import org.sensationcraft.login.SCLogin;

/**
 * Note that some of the methods use the database, thus they need to be called
 * async
 * @author DarkSeraphim
 */
public class SCLoginAPI 
{
    /**
     * Private getter for the SCLogin instance
     */
    private static SCLogin getPlugin()
    {
        return SCLogin.getInstance();
    }
    
    /**
     * Returns if the API is usable
     * @return true if the plugin was loaded in Bukkit
     */
    public static boolean isUsable()
    {
        return getPlugin() != null;
    }
    
    /**
     * Check if a player is registered. Call this async!
     */
    public static boolean isRegistered(String name)
    {
        return getPlugin().getPlayerManager().isRegistered(name);
    }
    
    /**
     * Check if a player is authenticated.
     * @param name - name of the Player
     * @return true if authenticated with /login, false otherwise
     */
    public static boolean isAuthenticated(String name)
    {
        return getPlugin().getPlayerManager().isLoggedIn(name);
    }
    
    /**
     * Check if the account is locked. Call this async!
     * @param name - name of the account
     * @return true if the account is known and locked, false otherwise
     */
    public static boolean isLocked(String name)
    {
        return getPlugin().getPlayerManager().isLocked(name);
    }
    
    /**
     * Sets the locking state of the account. Call this async!
     * @param name - name of the account
     * @param flag - state of the account (true means locked, false means unlocked)
     */
    public static void setLocked(String name, boolean flag)
    {
        getPlugin().getPlayerManager().setLocked(name, flag);
    }
    
    /**
     * Fetches the last IP the player used to authenticate himself. Call this async!
     * @param name - name of the Player
     * @return The IP, an empty String if nothing was found
     */
    public static String getIp(String name)
    {
        return getPlugin().getPlayerManager().getLastIp(name);
    }
}
