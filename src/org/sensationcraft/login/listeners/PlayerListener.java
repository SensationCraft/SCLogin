package org.sensationcraft.login.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.sensationcraft.login.SCLogin;

public class PlayerListener implements Listener
{
    
    private SCLogin plugin;
    
    public PlayerListener(SCLogin plugin)
    {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConsume(org.bukkit.event.player.PlayerItemConsumeEvent event)
    {
        if(!plugin.getPlayerManager().isLoggedIn(event.getPlayer().getName()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMove(org.bukkit.event.player.PlayerMoveEvent event)
    {
        if(!plugin.getPlayerManager().isLoggedIn(event.getPlayer().getName()))
        {
            event.setTo(event.getFrom());
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onTeleport(org.bukkit.event.player.PlayerTeleportEvent event)
    {
        if(!plugin.getPlayerManager().isLoggedIn(event.getPlayer().getName()))
        {
            //event.setTo(event.getFrom());
            //event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDealDamage(org.bukkit.event.entity.EntityDamageByEntityEvent event)
    {
        if(event.getDamager() instanceof Player == false) return;
        Player player = (Player) event.getDamager();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onTakeDamage(org.bukkit.event.entity.EntityDamageEvent event)
    {
        if(event.getEntity() instanceof Player == false) return;
        Player player = (Player) event.getEntity();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setDamage(0);
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(org.bukkit.event.block.BlockPlaceEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
            event.setBuild(false);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(org.bukkit.event.block.BlockBreakEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPickup(org.bukkit.event.player.PlayerPickupItemEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageItem(org.bukkit.event.player.PlayerItemDamageEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onDrop(org.bukkit.event.player.PlayerDropItemEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(org.bukkit.event.player.AsyncPlayerChatEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onCommand(org.bukkit.event.player.PlayerCommandPreprocessEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            String cmd = event.getMessage().split(" ")[0];
            if(!cmd.equals("/l") && !cmd.equalsIgnoreCase("/login") && !cmd.equalsIgnoreCase("/register"))
            {
                event.setCancelled(true);
                event.setMessage("/thiscommanddoesnotexistmate");
            }
        }
    }

}