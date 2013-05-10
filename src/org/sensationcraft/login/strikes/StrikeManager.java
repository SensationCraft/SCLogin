package org.sensationcraft.login.strikes;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.sensationcraft.login.SCLogin;

public class StrikeManager 
{
    private SCLogin plugin;
    
    Map<String, Integer> strikePoints = new HashMap<String, Integer>();
    
    private final int MAX_POINTS = 100;
    
    public StrikeManager(SCLogin plugin)
    {
        this.plugin = plugin;
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
        
        if(this.strikePoints.get(name) > MAX_POINTS)
        {
            resetStrikePoints(name);
            String ip = player.getAddress().getAddress().getHostAddress();
            // Make this a temp later
            Bukkit.banIP(ip);
        }
    }
    
    public void resetStrikePoints(String name)
    {
        this.strikePoints.remove(name);
    }
    
    
}
