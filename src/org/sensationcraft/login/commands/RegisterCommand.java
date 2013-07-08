package org.sensationcraft.login.commands;

import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

import com.google.common.collect.Sets;

public class RegisterCommand extends SCLoginMasterCommand
{

	private final SCLogin plugin;
	private final PlayerManager manager;
	private final Set<String> forbidden = Sets.newHashSet("123456", "password", "<password>");

	public RegisterCommand(final SCLogin scLogin)
	{
		super("register");
		this.plugin = scLogin;
		this.manager = this.plugin.getPlayerManager();
	}

	@Override
	public boolean execute(final CommandSender sender, final String[] args)
	{
		if (sender instanceof Player == false)
		{
			sender.sendMessage("This command can only be used by players");
			return true;
		}

		final int len = sender.getName().length();

		if (sender.getName().replaceAll("\\.\\*!", "").length() != len)
		{
			sender.sendMessage(Messages.USERNAME_BLACKLISTED.getMessage());
			return true;
		}

		if (args.length != 1)
		{
			sender.sendMessage(Messages.INVALID_SYNTAX.getMessage() + this.usage);
			return true;
		}

		if (args[0].length() < 6)
		{
			sender.sendMessage(Messages.PASSWORD_TOO_SHORT.getMessage());
			return true;
		}

		if (this.forbidden.contains(args[0].toLowerCase()))
		{
			sender.sendMessage(Messages.PASSWORD_BLACKLISTED.getMessage());
			return true;
		}

		final String ip = ((Player) sender).getAddress().getAddress().getHostAddress();

		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				try
				{
					if (RegisterCommand.this.manager.register(sender.getName(), args[0], ip))
					{
						sender.sendMessage(Messages.REGISTER_SUCCESS.getMessage().replace("%password%", args[0]));
						RegisterCommand.this.manager.join(sender.getName().toLowerCase(), true);
					}
					else
						sender.sendMessage(Messages.ALREADY_REGISTERED.getMessage());
				}
				catch (final Exception ex)
				{
					sender.sendMessage(ChatColor.RED + "An error occurred while attempting to register you. Please contact a member of staff on sensationcraft.info");
				}
			}
		}.runTaskAsynchronously(this.plugin);
		return true;
	}
}
