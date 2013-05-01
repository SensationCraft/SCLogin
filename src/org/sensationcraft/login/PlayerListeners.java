package org.sensationcraft.login;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListeners implements Listener{

	private Set<String> awaitingLogin = Collections.synchronizedSet(new HashSet<String>());
	private SCLogin scLogin;
	
	public PlayerListeners(SCLogin scLogin){
		this.scLogin = scLogin;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerLogin(AsyncPlayerPreLoginEvent e){
		String name = e.getName();
		if(!name.matches("[a-z*A-Z*0-9*_]")){
			e.disallow(Result.KICK_OTHER, "Your username contains illegal characters.");
			return;
		}
		if(isOnline(e.getName())){
			e.disallow(Result.KICK_OTHER, "You are already online!");
			return;
		}
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e){
		this.awaitingLogin.add(e.getPlayer().getName());
	}
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent e){
		this.awaitingLogin.remove(e.getPlayer().getName());
	}
	
	synchronized private boolean isOnline(String name){
		Player[] players = this.scLogin.getServer().getOnlinePlayers();
		for(Player player:players) if(player.getName().equalsIgnoreCase(name)) return true;
		return false;
	}
}
