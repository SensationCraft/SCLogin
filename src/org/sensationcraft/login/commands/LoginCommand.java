package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;

public class LoginCommand extends SCLoginMasterCommand
{

	private SCLogin plugin;

	private PlayerManager manager;

	public LoginCommand(SCLogin plugin)
	{
		this.plugin = plugin;
		this.manager = this.plugin.getPlayerManager();
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args)
        {
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
                
                final String ip = ((Player)sender).getAddress().getAddress().getHostAddress();

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(!LoginCommand.this.manager.isRegistered(sender.getName()))
				{
					sender.sendMessage(ChatColor.RED+"That account is not registered. Use /register <password> <confirm password> to register");
					return;
				}
				if(LoginCommand.this.plugin.getPasswordManager().checkPassword(sender.getName(), args[0], ip))
                                {
					LoginCommand.this.plugin.getPlayerManager().doLogin(sender.getName());
				}
                                else
                                {
                                    sender.sendMessage(ChatColor.RED+"Incorrect password!");
                                    return;
                                }
			}
		}.runTaskAsynchronously(this.plugin);

		return true;

	}

}
