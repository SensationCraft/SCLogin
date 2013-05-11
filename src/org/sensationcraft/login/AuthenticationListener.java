package org.sensationcraft.login;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class AuthenticationListener implements Listener{

	private SCLogin scLogin;

	public AuthenticationListener(SCLogin scLogin){
		this.scLogin = scLogin;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerLogin(AsyncPlayerPreLoginEvent e)
	{
		String name = e.getName();
                Bukkit.broadcastMessage(String.format("Yes, %s joined", name));
		if(!name.matches("[a-z*A-Z*0-9*_]"))
		{
			e.disallow(Result.KICK_OTHER, "Your username contains illegal characters.");
			return;
		}
		if(this.scLogin.getPlayerManager().isOnline(e.getName()))
		{
			e.disallow(Result.KICK_OTHER, "You are already online!");
			return;
		}
                Bukkit.broadcastMessage("Checking if registered");
		if(this.scLogin.getPlayerManager().isRegistered(name))
		{
                        Bukkit.broadcastMessage("Which seems to be the case");
			String ip = this.scLogin.getPlayerManager().getLastIp(name);
                        if(this.scLogin.getStrikeManager().isIpLockedout(ip))
                        {
                            e.disallow(Result.KICK_OTHER, "Your ip is locked out because you surpassed the amount of tries when entering your password");
                            return;
                        }
                        Bukkit.broadcastMessage(ip+":"+e.getAddress().getHostAddress());
                        String email = this.scLogin.getPlayerManager().getEmail(name);
                        email = "derp";
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
			}
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e)
	{
		final Player player = e.getPlayer();
		this.scLogin.getPlayerManager().join(player.getName());
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(AuthenticationListener.this.scLogin.getPlayerManager().isRegistered(player.getName()))
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
		}.runTaskLaterAsynchronously(this.scLogin, 1L);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e)
	{
		this.scLogin.getPlayerManager().quit(e.getPlayer().getName());
	}
}
