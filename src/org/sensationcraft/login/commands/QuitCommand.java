package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;

public class QuitCommand extends SCLoginMasterCommand
{

	private SCLogin plugin;

	private PlayerManager manager;

	public QuitCommand(SCLogin plugin)
	{
		this.plugin = plugin;
		this.manager = this.plugin.getPlayerManager();
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args)
        {
                this.plugin.logTiming("/quit for %s starting", sender.getName());
		if(sender instanceof Player == false)
		{
			sender.sendMessage("This command can only be used by players");
			return true;
		}
                
		((Player)sender).kickPlayer(ChatColor.GREEN+"Client closed the connection.");
                this.plugin.logTiming("/quit for %s ending command, please continue", sender.getName());
		return true;

	}

}