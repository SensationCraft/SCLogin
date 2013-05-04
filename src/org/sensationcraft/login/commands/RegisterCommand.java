package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;

public class RegisterCommand extends SCLoginMasterCommand{

	private SCLogin scLogin;
	private PlayerManager manager;
	
	public RegisterCommand(SCLogin scLogin){
		this.scLogin = scLogin;
		this.manager = this.scLogin.getPlayerManager();
	}
	
	@Override
	public boolean execute(final CommandSender sender, String[] args) {
		if(sender instanceof Player == false)
        {
            sender.sendMessage("This command can only be used by players");
            return true;
        }
        
        if(args.length != 1)
        {
            sender.sendMessage(ChatColor.RED+"Correct usage: /login <password>");
            return true;
        }
		new BukkitRunnable(){

			@Override
			public void run() {
				if(manager.isRegistered(sender.getName())){
					sender.sendMessage(ChatColor.RED+"I'm sorry, you are already registered. You can use '/changepw' to change your password.");
					return;
				}
				//TODO register player
			}
			
		}.runTaskAsynchronously(this.scLogin);
		return true;
	}

}
