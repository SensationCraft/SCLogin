package org.sensationcraft.login.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.SCLogin;

public class SCLoginCommand extends SCLoginMasterCommand{

	private SCLogin plugin;
	
	public SCLoginCommand(SCLogin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(CommandSender sender, String[] args) {
		// TODO Auto-generated method stub
		new BukkitRunnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				
			}
			
		}.runTaskAsynchronously(this.plugin);
		return false;
	}

}
