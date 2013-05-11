package org.sensationcraft.login.strikes;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.sql.Database;

public class StrikeManager 
{
    private SCLogin plugin;
    
    private PreparedStatement lockip;
    
    private final Object lockipLock = new Object();
    
    private PreparedStatement checkip;
    
    private final Object checkipLock = new Object();
    
    private PreparedStatement dellock;
    
    private final Object dellockLock = new Object();
    
    Map<String, Integer> strikePoints = new HashMap<String, Integer>();
    
    private final int MAX_POINTS = 100;
    
    private final int MAX_TRY_POINTS = 300;
    
    private final int TEMP_LOCKOUT = 1000*3600;
    
    public StrikeManager(SCLogin plugin)
    {
        this.plugin = plugin;
        this.lockip = this.plugin.getConnection().prepare("INSERT INTO `lockouts`(`ip`, `till`) VALUES(?,?)");
        this.checkip = this.plugin.getConnection().prepare("SELECT `till` FROM `lockouts` WHERE `ip` = ?");
        this.dellock = this.plugin.getConnection().prepare("DELETE FROM `lockouts` WHERE `ip` = ?");
    }
    
    public void addStrikePoints(Player player, int points)
    {
        String name = player.getName();
        if(!this.strikePoints.containsKey(name))
        {
            this.strikePoints.put(name, points);
        }
        else
        {
            this.strikePoints.put(name, points + this.strikePoints.get(name));
        }
        
        String ip = player.getAddress().getAddress().getHostAddress();
        if(this.strikePoints.get(name) > MAX_TRY_POINTS)
        {
            Bukkit.banIP(ip);
        }
        else if(this.strikePoints.get(name) > MAX_POINTS)
        {
            resetStrikePoints(name);
            Database.synchronizedExecuteUpdate(lockip, lockipLock, new java.sql.Timestamp(System.currentTimeMillis()+TEMP_LOCKOUT), ip);
        }
    }
    
    public void resetStrikePoints(String name)
    {
        this.strikePoints.remove(name);
    }
    
    public boolean isIpLockedout(final String ip)
    {
        ResultSet result = Database.synchronizedExecuteQuery(checkip, checkipLock, ip);
        try
        {
            if(result == null || !result.next()) return false;
            
            boolean done = result.getTimestamp("till").before(new Timestamp(System.currentTimeMillis()));
            if(done)
            {
                new BukkitRunnable()
                {
                    @Override
                    public void run()
                    {
                        Database.synchronizedExecuteUpdate(dellock, dellockLock, ip);
                    }
                }.runTaskAsynchronously(this.plugin);
            }
            return done;
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
        return false;
    }
}
