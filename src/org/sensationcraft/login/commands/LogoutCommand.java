package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;

public class LogoutCommand extends SCLoginMasterCommand
{

	private SCLogin plugin;

	private PlayerManager manager;

	public LogoutCommand(SCLogin plugin)
	{
		this.plugin = plugin;
		this.manager = this.plugin.getPlayerManager();
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args)
        {
                this.plugin.logTiming("/logout for %s end", sender.getName());
		if(sender instanceof Player == false)
		{
                        this.plugin.logTiming("/logout for %s end, not a player", sender.getName());
			sender.sendMessage("This command can only be used by players");
			return true;
		}
                this.plugin.logTiming("/logout for %s starting async", sender.getName());
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
                                LogoutCommand.this.plugin.logTiming("/logout for %s ending command async", sender.getName());
                                LogoutCommand.this.manager.quit(sender.getName().toLowerCase());
                                sender.sendMessage(ChatColor.GREEN+"You are no longer logged in.");
			}
		}.runTaskAsynchronously(this.plugin);
                this.plugin.logTiming("/login for %s endng command, please continue", sender.getName());
		return true;

	}

}
