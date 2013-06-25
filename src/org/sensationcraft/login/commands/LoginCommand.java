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
                this.plugin.logTiming("/login for %s", sender.getName());
		if(sender instanceof Player == false)
		{
                        this.plugin.logTiming("/login for %s end", sender.getName());
			sender.sendMessage("This command can only be used by players");
			return true;
		}

		if(args.length != 1)
		{
                        this.plugin.logTiming("/login for %s end", sender.getName());
			sender.sendMessage(ChatColor.RED+"Correct usage: /login <password>");
			return true;
		}
                
                final String ip = ((Player)sender).getAddress().getAddress().getHostAddress();

                this.plugin.logTiming("/login for %s, start asyncing", sender.getName());
                
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if(!LoginCommand.this.manager.isRegistered(sender.getName().toLowerCase()))
				{
                                        LoginCommand.this.plugin.logTiming("/login for %s ending async, already registered", sender.getName());
					sender.sendMessage(ChatColor.RED+"That account is not registered. Use /register <password> <confirm password> to register");
					return;
				}
				if(LoginCommand.this.plugin.getPasswordManager().checkPassword(sender.getName().toLowerCase(), args[0], ip))
                                {
					LoginCommand.this.plugin.getPlayerManager().doLogin(sender.getName().toLowerCase());
                                        LoginCommand.this.plugin.getStrikeManager().resetStrikePoints(sender.getName().toLowerCase(), true);
				}
                                else
                                {
                                    sender.sendMessage(ChatColor.RED+"Incorrect password!");
                                    plugin.getStrikeManager().addStrikePoints((Player)sender, 34, true);
                                }
                                LoginCommand.this.plugin.logTiming("/login for %s ending command async", sender.getName());
			}
		}.runTaskAsynchronously(this.plugin);
                this.plugin.logTiming("/login for %s ending command, please continue", sender.getName());
		return true;

	}

}
