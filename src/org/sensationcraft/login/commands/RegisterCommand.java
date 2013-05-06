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
	public boolean execute(final CommandSender sender, final String[] args) 
        {
		if(sender instanceof Player == false)
		{
			sender.sendMessage("This command can only be used by players");
			return true;
		}

		if(args.length != 2)
		{
			sender.sendMessage(ChatColor.RED+"Correct usage: /register <password> <confirm password>");
			return true;
		}
                
                if(!args[0].equals(args[1]))
                {
                        sender.sendMessage(ChatColor.RED+"Your entered passwords don't seem to match.");
                        return true;
                }
                
                final String ip = ((Player)sender).getAddress().getAddress().getHostAddress();
                
		new BukkitRunnable()
                {

			@Override
			public void run() 
                        {
                            try
                            {
				if(RegisterCommand.this.manager.register(sender.getName(), args[1], ip))
                                {
					sender.sendMessage(ChatColor.GREEN+"You are now registered, use /login <password> to login.");
				}
                                else
                                {
                                        sender.sendMessage(ChatColor.RED+"I'm sorry, you are already registered. You can use '/changepw' to change your password.");
                                }
                            }
                            catch(Exception ex)
                            {
                                sender.sendMessage(ChatColor.RED+"An error occurred while attempting to register you. Please contact a member of staff on sensationcraft.info");
                            }
			}

		}.runTaskAsynchronously(this.scLogin);
		return true;
	}

}
