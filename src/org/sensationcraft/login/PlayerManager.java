package org.sensationcraft.login;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.messages.Messages;
import org.sensationcraft.login.sql.Database;

public class PlayerManager
{

    public enum Status
    {
        NOT_LOGGED_IN, AUTHENTICATED;
    }
    public Map<String, Status> playerStatus = new HashMap<String, Status>();
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
    private PreparedStatement updateIp;
    private final Object updateIpLock = new Object();
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
    
    private File safegaurdFile;
    private YamlConfiguration safegaurdCfg;
    private final Set<String> safeguarded = new HashSet<String>();
    private final Object safegaurdLock = new Object();

    protected PlayerManager(SCLogin plugin)
    {
        this.plugin = plugin;
        this.registered = this.plugin.getConnection().prepare("SELECT * FROM `players` WHERE LOWER(`username`) = LOWER(?)");
        this.ip = this.plugin.getConnection().prepare("SELECT `lastip` FROM `players` WHERE LOWER(`username`) = LOWER(?)");
        this.updateIp = this.plugin.getConnection().prepare("UPDATE `players` SET `lastip` = ? WHERE LOWER(`username`) = LOWER(?)");
        this.register = this.plugin.getConnection().prepare("INSERT INTO `players`(`username`, `password`, `lastip`, `email`, `locked`) VALUES(LOWER(?), ?, ?, ?, ?)");
        this.email = this.plugin.getConnection().prepare("SELECT `email` FROM `players` WHERE LOWER(`username`) = LOWER(?)");
        this.unregister = this.plugin.getConnection().prepare("DELETE FROM `players` WHERE LOWER(`username`) = LOWER(?)");
        this.isLocked = this.plugin.getConnection().prepare("SELECT locked FROM `players` WHERE LOWER(`username`) = LOWER(?)");
        this.setLock = this.plugin.getConnection().prepare("UPDATE `players` SET `locked` = ? WHERE LOWER(`username`) = LOWER(?)");
        this.getActiveCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` > 0");
        this.getLockedCount = this.plugin.getConnection().prepare("SELECT COUNT(*) as count FROM `players` WHERE `locked` = 0");
        
        this.safegaurdFile = new File(plugin.getDataFolder(), "safegaurd.dat");
        this.safegaurdCfg = YamlConfiguration.loadConfiguration(safegaurdFile);
        this.safeguarded.addAll(this.safegaurdCfg.getStringList("locked-to-ip"));
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
        catch (SQLException ex)
        {
            // Might log this
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
        return false;
    }

    public String getLastIp(String name)
    {
        ResultSet result = null;
        try
        {
            result = Database.synchronizedExecuteQuery(this.ip, this.ipLock, name);
            if (!result.next())
            {
                return "";
            }
            return result.getString("lastip");
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
        return "";
    }

    public String getEmail(String name)
    {
        ResultSet result = null;
        try
        {
            result = Database.synchronizedExecuteQuery(email, emailLock, name);
            if (!result.next())
            {
                return "";
            }
            return result.getString("email");
        }
        catch (SQLException ex)
        {
            // Might log this
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
        return "";
    }

    public boolean isLoggedIn(String name)
    {
        synchronized (this.statusLock)
        {
            return this.playerStatus.get(name.toLowerCase()) == Status.AUTHENTICATED;
        }
    }

    public boolean isOnline(String name)
    {
        synchronized (this.statusLock)
        {
            return this.playerStatus.get(name.toLowerCase()) == Status.AUTHENTICATED;
        }
    }

    public void join(String name)
    {
        synchronized (this.statusLock)
        {
            this.playerStatus.put(name.toLowerCase(), Status.NOT_LOGGED_IN);
        }
    }

    public void quit(String name)
    {
        synchronized (this.statusLock)
        {
            this.playerStatus.remove(name.toLowerCase());
        }
    }

    public void doLogin(String name)
    {
        final Player player = this.plugin.getServer().getPlayerExact(name);
        if (player == null)
        {
            this.quit(name);
            return;
        }
        synchronized (this.statusLock)
        {
            this.playerStatus.put(name, Status.AUTHENTICATED);
        }
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.sendMessage(Messages.LOGIN_SUCCESS.getMessage());
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                for (Player other : Bukkit.getOnlinePlayers())
                {
                    if (!other.canSee(player) && isLoggedIn(other.getName().toLowerCase()))
                    {
                        other.showPlayer(player);
                    }
                }
            }
        }.runTask(this.plugin);
        Database.synchronizedExecuteUpdate(updateIp, updateIpLock, player.getAddress().getAddress().getHostAddress(), name);
    }

    public boolean register(String name, String pass, String ip) throws Exception
    {
        if (this.isRegistered(name))
        {
            return false;
        }

        Database.synchronizedExecuteUpdate(this.register, this.registerLock, name, pass, ip, 0);

        return this.isRegistered(name);
    }

    public boolean unregister(String name)
    {
        if (!this.isRegistered(name))
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
            if (result == null || !result.next())
            {
                return false;
            }
            return result.getInt("locked") > 0;
        }
        catch (SQLException ex)
        {
            ex.printStackTrace();
            // Swallow the exception
        }
        return false;
    }

    public boolean setLocked(String name, boolean flag)
    {
        Database.synchronizedExecuteUpdate(this.setLock, this.setLockLock, (flag ? 1 : 0), name);
        return isLocked(name);
    }
    
    public boolean setLockedToIp(String name, boolean flag)
    {
        name = name.toLowerCase();
        synchronized(this.safegaurdLock)
        {
            if(flag == this.safeguarded.contains(name))
            {
                return false;
            }
            if(flag) this.safeguarded.add(name);
            else this.safeguarded.remove(name);
        }
        this.safegaurdCfg.set("locked-to-ip", new ArrayList<String>(this.safeguarded));
        
        try
        {
            this.safegaurdCfg.save(this.safegaurdFile);
        }
        catch(Exception ex)
        {
            // Swallow the exception
        }
        return true;
    }
    
    public boolean isLockedToIp(String name)
    {
        synchronized(this.safegaurdLock)
        {
            return this.safeguarded.contains(name.toLowerCase());
        }
    }

    public String getProfile(String name)
    {
        if (!this.isRegistered(name))
        {
            return ChatColor.RED + "This player has not played before.";
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
        if (what == null)
        {
            return null;
        }
        ResultSet result = null;
        if (what.startsWith("a"))
        {
            result = Database.synchronizedExecuteQuery(this.getActiveCount, this.getActiveCountLock);
        }
        else if (what.startsWith("l"))
        {
            result = Database.synchronizedExecuteQuery(this.getLockedCount, this.getLockedCountLock);
        }
        try
        {
            if (result != null && result.next())
            {
                return String.format("%d", result.getInt("count"));
            }
        }
        catch (SQLException ex)
        {
            // Swallow the exception
        }
        catch (IllegalFormatException ex)
        {
            // Swallow the exception
        }
        return null;
    }
}
