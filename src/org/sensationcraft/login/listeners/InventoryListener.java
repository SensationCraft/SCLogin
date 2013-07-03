package org.sensationcraft.login.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.sensationcraft.login.SCLogin;

public class InventoryListener implements Listener
{

	private final SCLogin plugin;

	public InventoryListener(final SCLogin plugin)
	{
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onClick(final org.bukkit.event.inventory.InventoryClickEvent event)
	{
		if (event.getWhoClicked() instanceof Player == false)
			return;
		final Player player = (Player) event.getWhoClicked();
		if (this.plugin.getPlayerManager().isLoggedIn(player.getName().toLowerCase()))
			return;

		event.setCancelled(true);
		event.setResult(Event.Result.DENY);

		this.plugin.getStrikeManager().addStrikePoints(player, 34, false);
	}
        
        @EventHandler(priority = EventPriority.HIGHEST)
	public void onDrag(final org.bukkit.event.inventory.InventoryDragEvent event)
	{
		if (event.getWhoClicked() instanceof Player == false)
			return;
		final Player player = (Player) event.getWhoClicked();
		if (this.plugin.getPlayerManager().isLoggedIn(player.getName().toLowerCase()))
			return;

		event.setCancelled(true);
		event.setResult(Event.Result.DENY);

		this.plugin.getStrikeManager().addStrikePoints(player, 34, false);
	}
}
