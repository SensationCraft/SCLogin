package org.sensationcraft.login.commands;

import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

public class RegisterCommand extends SCLoginMasterCommand
{

    private SCLogin plugin;
    private PlayerManager manager;
    private final Set<String> forbidden = Sets.newHashSet("123456", "password", "<password>");

    public RegisterCommand(SCLogin scLogin)
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

        int len = sender.getName().length();

        if (sender.getName().replaceAll("_-\\.\\*!", "").length() != len)
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
                    if (RegisterCommand.this.manager.register(sender.getName().toLowerCase(), args[1], ip))
                    {
                        sender.sendMessage(Messages.REGISTER_SUCCESS.getMessage());
                    }
                    else
                    {
                        sender.sendMessage(Messages.ALREADY_REGISTERED.getMessage());
                    }
                }
                catch (Exception ex)
                {
                    sender.sendMessage(ChatColor.RED + "An error occurred while attempting to register you. Please contact a member of staff on sensationcraft.info");
                }
            }
        }.runTaskAsynchronously(this.plugin);
        return true;
    }
}
