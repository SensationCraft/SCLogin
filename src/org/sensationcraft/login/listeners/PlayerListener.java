package org.sensationcraft.login.listeners;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

public class PlayerListener implements Listener
{

	private final SCLogin plugin;

	private final Map<String, Integer> count = new HashMap<String, Integer>();
        
        private final PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2);

	public PlayerListener(final SCLogin plugin)
	{
		this.plugin = plugin;
	}

	public boolean count(String name)
	{
		name = name.toLowerCase();
		Integer i = this.count.get(name);
		if(i == null)
			i = 0;
		else
		{
			i++;
			i %= 3;
		}
		this.count.put(name, i);
		return i == 0;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onConsume(final org.bukkit.event.player.PlayerItemConsumeEvent event)
	{
		if(!this.plugin.getPlayerManager().isLoggedIn(event.getPlayer().getName()))
		{
			event.setCancelled(true);
			this.plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 50, false);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onMove(final org.bukkit.event.player.PlayerMoveEvent event)
	{
		final String name = event.getPlayer().getName();
		if(!this.plugin.getPlayerManager().isLoggedIn(name))
		{
			event.setTo(event.getFrom());
			if(this.count(name) && !this.plugin.getPlayerManager().isLoggingIn(event.getPlayer().getName()))
				event.getPlayer().sendMessage(this.plugin.getPlayerManager().hasRegistered(name) ? Messages.NOT_LOGGEDIN.getMessage() : Messages.NOT_REGISTERED_YET.getMessage());
			//event.setCancelled(true);
			//plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 1, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTeleport(final org.bukkit.event.player.PlayerTeleportEvent event)
	{
		if(!this.plugin.getPlayerManager().isLoggedIn(event.getPlayer().getName()))
		{
			//event.setTo(event.getFrom());
			//event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDealDamage(final org.bukkit.event.entity.EntityDamageByEntityEvent event)
	{
		if(event.getDamager() instanceof Player == false) return;
		final Player player = (Player) event.getDamager();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setDamage(0.0);
			event.setCancelled(true);
			this.plugin.getStrikeManager().addStrikePoints(player, 10, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTakeDamage(final org.bukkit.event.entity.EntityDamageEvent event)
	{
		if(event.getEntity() instanceof Player == false) return;
		final Player player = (Player) event.getEntity();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setDamage(0.0);
			event.setCancelled(true);
			this.plugin.getStrikeManager().addStrikePoints(player, 10, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onInteract(final org.bukkit.event.player.PlayerInteractEvent event)
	{
		final Player player = event.getPlayer();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setCancelled(true);
			this.plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 10, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlace(final org.bukkit.event.block.BlockPlaceEvent event)
	{
		final Player player = event.getPlayer();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setCancelled(true);
			event.setBuild(false);
			this.plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 10, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBreak(final org.bukkit.event.block.BlockBreakEvent event)
	{
		final Player player = event.getPlayer();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setCancelled(true);
			this.plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 10, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPickup(final org.bukkit.event.player.PlayerPickupItemEvent event)
	{
		final Player player = event.getPlayer();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
			event.setCancelled(true);
	}

	/*@EventHandler(priority = EventPriority.LOWEST)
    public void onDamageItem(org.bukkit.event.player.PlayerItemDamageEvent event)
    {
        Player player = (Player) event.getPlayer();
        if(!plugin.getPlayerManager().isLoggedIn(player.getName()))
        {
            event.setCancelled(true);
            plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 10, false);
        }
    }*/

	@EventHandler(priority = EventPriority.LOWEST)
	public void onDrop(final org.bukkit.event.player.PlayerDropItemEvent event)
	{
		final Player player = event.getPlayer();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setCancelled(true);
			this.plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 10, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(final org.bukkit.event.player.AsyncPlayerChatEvent event)
	{
		final Player player = event.getPlayer();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setCancelled(true);
			this.plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 10, false);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onCommand(final org.bukkit.event.player.PlayerCommandPreprocessEvent event)
	{
		final Player player = event.getPlayer();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			final String cmd = event.getMessage().split(" ")[0];
			if(!cmd.equals("/l") && !cmd.equalsIgnoreCase("/login") && !cmd.equalsIgnoreCase("/register"))
			{
				event.setCancelled(true);
				event.setMessage("/thiscommanddoesnotexistmate");
				this.plugin.getStrikeManager().addStrikePoints(event.getPlayer(), 20, false);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onHeal(final EntityRegainHealthEvent event)
	{
		if(event.getEntity() instanceof Player == false) return;
		final Player player = (Player) event.getEntity();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
		{
			event.setAmount(0.0);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onFeed(final FoodLevelChangeEvent event)
	{
		if(event.getEntity() instanceof Player == false) return;
		final Player player = (Player) event.getEntity();
		if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
			event.setCancelled(true);
	}

	@EventHandler
	public void onQuit(final PlayerQuitEvent event)
	{
		final String name = event.getPlayer().getName().toLowerCase();
		this.count.remove(name);
	}
        
        @EventHandler
        public void onRespawn(final PlayerRespawnEvent event)
        {
            Player player = event.getPlayer();
            if(!this.plugin.getPlayerManager().isLoggedIn(player.getName()))
            {
                player.addPotionEffect(this.blindness);
            }
        }
}
