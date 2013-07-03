package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PlayerManager;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

public class LogoutCommand extends SCLoginMasterCommand
{

    private SCLogin plugin;
    private PlayerManager manager;

    public LogoutCommand(SCLogin plugin)
    {
        super("logout");
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
        new BukkitRunnable()
        {
            @Override
            public void run()
            {
                sender.sendMessage(Messages.LOGOUT.getMessage());
                LogoutCommand.this.manager.quit(sender.getName().toLowerCase());
            }
        }.runTaskAsynchronously(this.plugin);
        return true;

    }
}
