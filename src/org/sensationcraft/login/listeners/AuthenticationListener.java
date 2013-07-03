package org.sensationcraft.login.listeners;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

public class AuthenticationListener implements Listener
{

	private final SCLogin plugin;
	private final PotionEffect blindness = new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 2);
	private final Map<String, String> kicked = new HashMap<String, String>();
	private final Object kickedLock = new Object();
	private final Set<String> joined = new HashSet<String>();
	private final Object joinedLock = new Object();

	public AuthenticationListener(final SCLogin scLogin)
	{
		this.plugin = scLogin;
	}

	/**
	 * Fix for offline mode and the calling of the async prelogin event
	 */
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerLogin(final PlayerLoginEvent event)
	{
		if (Bukkit.getOnlineMode())
			return;
		final Player player = event.getPlayer();
		final String name = player.getName();
		final InetAddress address = event.getAddress();

		if (this.plugin.getPlayerManager().isOnline(name))
		{
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You are already online!");
			return;
		}

		if (name.trim().length() < 3)
		{
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your name has to have at least three characters");
			return;
		}
		if (!name.matches("[a-zA-Z0-9_]*"))
		{
			event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "Your username contains illegal characters.");
			return;
		}

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				final AsyncPlayerPreLoginEvent event = new AsyncPlayerPreLoginEvent(name, address);
				Bukkit.getPluginManager().callEvent(event);
				if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
				{
					boolean ready = false;
					synchronized (AuthenticationListener.this.joinedLock)
					{
						ready = AuthenticationListener.this.joined.remove(name);
					}
					if (ready)
						new BukkitRunnable()
					{
						@Override
						public void run()
						{
							player.kickPlayer(event.getKickMessage());
						}
					}.runTask(AuthenticationListener.this.plugin);
					else
						synchronized (AuthenticationListener.this.kickedLock)
						{
							AuthenticationListener.this.kicked.put(name, event.getKickMessage());
						}
				}
			}
		}.runTaskAsynchronously(this.plugin);
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(final AsyncPlayerPreLoginEvent e)
	{
		final String name = e.getName().toLowerCase();
		if (this.plugin.getPlayerManager().isOnline(name))
		{
			e.disallow(Result.KICK_OTHER, "You are already online!");
			return;
		}
                boolean registered = false;
		if (this.plugin.getPlayerManager().isRegistered(name))
		{
                        registered = true;
			final String ip = this.plugin.getPlayerManager().getLastIp(name);
			if (this.plugin.getStrikeManager().isIpLockedout(ip))
			{
				e.disallow(Result.KICK_OTHER, Messages.IP_LOCKOUT.getMessage());
				return;
			}

			final boolean lockIP = this.plugin.getPlayerManager().isLockedToIp(name);
			if (!ip.equals(e.getAddress().getHostAddress()) && lockIP)
			{
				e.disallow(Result.KICK_OTHER, Messages.IP_DOESNT_MATCH.getMessage());
				return;
			}
		}
		this.plugin.getPlayerManager().join(name, registered);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(final PlayerJoinEvent e)
	{
		final Player player = e.getPlayer();
		player.addPotionEffect(this.blindness);
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (AuthenticationListener.this.plugin.getPlayerManager().isRegistered(player.getName().toLowerCase()))
					player.sendMessage(Messages.WELCOME_BACK.getMessage());
				else player.sendMessage(Messages.NEW_PLAYER.getMessage());
			}
		}.runTaskLaterAsynchronously(this.plugin, 1L);
		for (final Player other : Bukkit.getOnlinePlayers())
		{
			if (other.canSee(player))
				other.hidePlayer(player);
			if (player.canSee(other))
				player.hidePlayer(other);
		}

		String reason = null;
		synchronized (this.kickedLock)
		{
			reason = this.kicked.remove(player.getName());
		}
		if (reason != null)
			player.kickPlayer(reason);
		else
			synchronized (this.joinedLock)
			{
				this.joined.add(player.getName());
			}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(final PlayerQuitEvent e)
	{
		this.plugin.getPlayerManager().quit(e.getPlayer().getName().toLowerCase());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onKick(final PlayerKickEvent event)
	{
		final String name = event.getPlayer().getName().toLowerCase();
		final PlayerManager pm = this.plugin.getPlayerManager();
		if (event.getReason().startsWith("Kicked for flying") && pm.isOnline(name) && !pm.isLoggedIn(name))
			event.setCancelled(true);

	}
}
