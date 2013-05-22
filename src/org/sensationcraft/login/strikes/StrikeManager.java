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
    
    Map<String, Integer> lowPriority = new HashMap<String, Integer>();
    
    Map<String, Integer> highPriority = new HashMap<String, Integer>();
    
    private final int MAX_POINTS = 100;
        
    private final int TEMP_LOCKOUT = 1000*3600;
    
    private final String timeout;
    
    public StrikeManager(SCLogin plugin)
    {
        this.plugin = plugin;
        this.lockip = this.plugin.getConnection().prepare("INSERT INTO `lockouts`(`ip`, `till`) VALUES(?,?)");
        this.checkip = this.plugin.getConnection().prepare("SELECT `till` FROM `lockouts` WHERE `ip` = ?");
        this.dellock = this.plugin.getConnection().prepare("DELETE FROM `lockouts` WHERE `ip` = ?");
        StringBuilder to = new StringBuilder();
        int y = (int) Math.floor(TEMP_LOCKOUT / (1000*3600*24*365));
        int w = (int) Math.floor((TEMP_LOCKOUT - (1000*3600*24*365*y))/(1000*3600*24*7));
        int d = (int) Math.floor((TEMP_LOCKOUT - (1000*3600*24*365*y) - (1000*3600*24*7*w)) / (1000*3600*24));
        int h = (int) Math.floor((TEMP_LOCKOUT - (1000*3600*24*365*y) - (1000*3600*24*7*w) - (1000*3600*24*d)) / (1000*3600));
        int m = (int) Math.floor(0);
        this.timeout = to.toString();
    }
    
    public void addStrikePoints(Player player, int points, boolean highPriority)
    {
        Map<String, Integer> strikePoints = highPriority ? this.highPriority : this.lowPriority;
        
        String name = player.getName();
        if(!strikePoints.containsKey(name))
        {
            strikePoints.put(name, points);
        }
        else
        {
            strikePoints.put(name, points + strikePoints.get(name));
        }
        
        String ip = player.getAddress().getAddress().getHostAddress();
        if(strikePoints.get(name) > MAX_POINTS)
        {
            resetStrikePoints(name, highPriority);
            if(highPriority)
            {
                Database.synchronizedExecuteUpdate(lockip, lockipLock, new java.sql.Timestamp(System.currentTimeMillis()+TEMP_LOCKOUT), ip);
                
                player.kickPlayer(String.format("Your ip has been banned for %s", this.timeout));
            }
            else
            {
                player.kickPlayer("You are required to log in first!");
            }
        }
    }
    
    public void resetStrikePoints(String name, boolean highPriority)
    {
        if(highPriority) this.highPriority.remove(name);
        this.lowPriority.remove(name);
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
