package org.sensationcraft.login.commands;

import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PasswordManager;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

public class ChangePasswordCommand extends SCLoginMasterCommand
{

    private SCLogin plugin;
    private final Set<String> forbidden = Sets.newHashSet("123456", "password", "<password>");

    public ChangePasswordCommand(SCLogin plugin)
    {
        super("changepassword");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, final String[] args)
    {
        if (sender instanceof Player == false)
        {
            sender.sendMessage("This command can only be used by players");
            return true;
        }

        if (args.length != 3)
        {
            sender.sendMessage(Messages.INVALID_SYNTAX.toString() + this.usage);
            return true;
        }

        if (!args[1].equals(args[2]))
        {
            sender.sendMessage(Messages.PASSWORDS_DONT_MATCH.getMessage());
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

        final Player player = (Player) sender;

        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                String name = player.getName().toLowerCase();

                PasswordManager pwmanager = ChangePasswordCommand.this.plugin.getPasswordManager();

                if (pwmanager.checkPassword(name, args[0], player.getAddress().getAddress().getHostAddress()))
                {
                    pwmanager.changePassword(name, args[1]);
                    player.sendMessage(Messages.PASSWORD_CHANGED.toString());
                }
            }
        }.runTaskAsynchronously(this.plugin);
        return true;
    }
}
