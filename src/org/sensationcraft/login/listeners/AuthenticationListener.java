package org.sensationcraft.login.listeners;

import java.net.InetAddress;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;

public class AuthenticationListener implements Listener{

	private SCLogin plugin;

	public AuthenticationListener(SCLogin scLogin)
        {
		this.plugin = scLogin;
	}
        
        /**
         * Fix for offline mode and the calling of the async prelogin event
         */
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerLogin(PlayerLoginEvent event)
        {
            if(Bukkit.getOnlineMode()) return;
            final String name = event.getPlayer().getName();
            final InetAddress address = event.getAddress();
            final Player player = event.getPlayer();
            
            if(this.plugin.getPlayerManager().isOnline(name))
            {
                    event.disallow(PlayerLoginEvent.Result.KICK_OTHER, "You are already online!");
                    return;
            }
            
            new BukkitRunnable()
            {
                @Override
                public void run()
                {
                    final AsyncPlayerPreLoginEvent event = new AsyncPlayerPreLoginEvent(name, address);
                    Bukkit.getPluginManager().callEvent(event);
                    if(event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
                    {
                        // Not sure if this would be thread-safe
                        new BukkitRunnable()
                        {
                            @Override
                            public void run()
                            {
                                System.out.println(String.format("Kicked player %s for %s", player.getName(), event.getKickMessage()));
                                player.kickPlayer(event.getKickMessage());
                            }
                        }.runTask(AuthenticationListener.this.plugin);      
                    }
                }
            }.runTaskAsynchronously(plugin);
        }

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(AsyncPlayerPreLoginEvent e)
	{
		String name = e.getName().toLowerCase();
                if(name.trim().length() < 3)
                {
                    e.disallow(Result.KICK_OTHER, "Your name has to have at least three characters");
                    return;
                }
		if(!name.matches("[a-zA-Z0-9_]*"))
		{
			e.disallow(Result.KICK_OTHER, "Your username contains illegal characters.");
			return;
		}
		if(this.plugin.getPlayerManager().isOnline(name))
		{
			e.disallow(Result.KICK_OTHER, "You are already online!");
			return;
		}
		if(this.plugin.getPlayerManager().isRegistered(name))
		{
			String ip = this.plugin.getPlayerManager().getLastIp(name);
                        if(this.plugin.getStrikeManager().isIpLockedout(ip))
                        {
                            e.disallow(Result.KICK_OTHER, "Your ip is locked out because you surpassed the amount of tries when entering your password");
                            return;
                        }
                        String email = this.plugin.getPlayerManager().getEmail(name);
			if(!ip.equals(e.getAddress().getHostAddress()) && email != null && !email.isEmpty())
			{
				
				if(email == null || email.isEmpty())
				{
					e.disallow(Result.KICK_OTHER, "Your ip does not match with the last ip you authenticated with. As you do not have an email set, please contact a member of staff about this!");
				}
				else
				{
					//sendEmail();
					e.disallow(Result.KICK_OTHER, "Your ip does not match with the last ip you authenticated with. We sent an email to your inbox with a code to verify this is indeed you.");
				}
                                return;
			}
		}
                this.plugin.getPlayerManager().join(name);
	}
        
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player player = e.getPlayer();
                new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(AuthenticationListener.this.plugin.getPlayerManager().isRegistered(player.getName().toLowerCase()))
				{
					player.sendMessage(ChatColor.RED+"Welcome back to SensationCraft. Please login using /login <password>");
				}
				else
				{
					player.sendMessage(ChatColor.RED+"Welcome to SensationCraft. It does not appear you logged in before. Please register using /register <password> <password>");
				}
				// This is assumed to be thread-safe as it uses
				// a synchronized list for the chat packets
			}
		}.runTaskLaterAsynchronously(this.plugin, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		this.plugin.getPlayerManager().quit(e.getPlayer().getName().toLowerCase());
	}
        
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onKick(PlayerKickEvent event)
        {
            String name = event.getPlayer().getName().toLowerCase();
            PlayerManager pm = this.plugin.getPlayerManager();
            if(event.getReason().startsWith("Kicked for flying") && pm.isOnline(name) && !pm.isLoggedIn(name))
                event.setCancelled(true);

        }
        
}
