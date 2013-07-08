package org.sensationcraft.login.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.event.SCLoginLoginEvent;
import org.sensationcraft.login.messages.Messages;

public class LoginCommand extends SCLoginMasterCommand
{

	private final SCLogin plugin;
	private final PlayerManager manager;

	public LoginCommand(final SCLogin plugin)
	{
		super("login");
		this.plugin = plugin;
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

		final Player player = (Player) sender;

		if (args.length != 1)
		{
			sender.sendMessage(Messages.INVALID_SYNTAX.getMessage() + this.usage);
			return true;
		}

		final String ip = player.getAddress().getAddress().getHostAddress();

		final String name = player.getName();
		LoginCommand.this.manager.loggingIn.add(sender.getName());
		sender.sendMessage(Messages.LOGGING_IN.getMessage());
		new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (LoginCommand.this.manager.isLoggedIn(name))
				{
					sender.sendMessage(Messages.ALREADY_LOGGEDIN.getMessage());
					LoginCommand.this.manager.loggingIn.remove(sender.getName());
					return;
				}
				if (!LoginCommand.this.manager.isRegistered(name))
				{
					sender.sendMessage(Messages.NOT_REGISTERED.getMessage());
					LoginCommand.this.manager.loggingIn.remove(sender.getName());
					return;
				}
				if (LoginCommand.this.manager.isLocked(name))
				{
					sender.sendMessage(Messages.LOCKED.getMessage());
					LoginCommand.this.manager.loggingIn.remove(sender.getName());
					return;
				}
				if (LoginCommand.this.plugin.getPasswordManager().checkPassword(name, args[0], ip))
				{
					LoginCommand.this.plugin.getPlayerManager().doLogin(name);
					LoginCommand.this.plugin.getStrikeManager().resetStrikePoints(name, true);
					LoginCommand.this.manager.loggingIn.remove(sender.getName());

					new BukkitRunnable()
					{
						@Override
						public void run()
						{
                                                        Bukkit.getPluginManager().callEvent(new SCLoginLoginEvent(name, player));
							for (final Player other : Bukkit.getOnlinePlayers())
								if (other.isValid() && LoginCommand.this.plugin.getPlayerManager().isLoggedIn(other.getName()))
								{
									if(!other.canSee(player) && LoginCommand.this.plugin.getPlayerManager().isVisible(player))
										other.showPlayer(player);
									if(!player.canSee(other) && LoginCommand.this.plugin.getPlayerManager().isVisible(other))
										player.showPlayer(other);
								}
						}
					}.runTask(LoginCommand.this.plugin);

					new BukkitRunnable()
					{
						@Override
						public void run()
						{
							player.playSound(player.getLocation(), Sound.ORB_PICKUP, 10f, 0F);
						}
					}.runTask(LoginCommand.this.plugin);
				}
				else if (LoginCommand.this.plugin.getPlayerManager().isLocked(name))
				{
					sender.sendMessage(Messages.LOCKED.getMessage());
					LoginCommand.this.manager.loggingIn.remove(sender.getName());
				}
				else
				{
					sender.sendMessage(Messages.INCORRECT_PASSWORD.getMessage());
					LoginCommand.this.plugin.getStrikeManager().addStrikePoints((Player) sender, 21, true);
					LoginCommand.this.manager.loggingIn.remove(sender.getName());
				}
			}
		}.runTaskAsynchronously(this.plugin);
		return true;

	}
}
