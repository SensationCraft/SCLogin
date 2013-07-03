package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

public class UnregisterCommand extends SCLoginMasterCommand
{

	private final SCLogin scLogin;
	private final PlayerManager manager;

	public UnregisterCommand(final SCLogin scLogin)
	{
		super("unregister");
		this.scLogin = scLogin;
		this.manager = this.scLogin.getPlayerManager();
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args)
	{
		if (sender instanceof Player == false)
		{
			sender.sendMessage("This command can only be used by players");
			return true;
		}

		((Player) sender).getAddress().getAddress().getHostAddress();

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (UnregisterCommand.this.manager.unregister(sender.getName().toLowerCase()))
					{
						sender.sendMessage(Messages.UNREGISTER_SUCCESS.getMessage());
						UnregisterCommand.this.manager.quit(sender.getName().toLowerCase());
					} else
						sender.sendMessage(Messages.UNREGISTER_FAILED.getMessage());
				}
				catch (final Exception ex)
				{
					sender.sendMessage(ChatColor.RED + "An error occurred while attempting to unregister you. Please contact a member of staff on sensationcraft.info");
				}
			}
		}.runTaskAsynchronously(this.scLogin);
		return true;
	}
}
