package org.sensationcraft.login;

import org.bukkit.plugin.java.JavaPlugin;

public class SCLogin extends JavaPlugin{

	@Override
	public void onEnable(){
		this.getLogger().info("Registering listeners...");
		this.getServer().getPluginManager().registerEvents(new PlayerListeners(this), this);
	}

}
