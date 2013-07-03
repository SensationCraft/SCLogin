package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sensationcraft.login.SCLogin;

public class QuitCommand extends SCLoginMasterCommand
{

    private SCLogin plugin;

    public QuitCommand(SCLogin plugin)
    {
        super("quit");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(final CommandSender sender, final String[] args)
    {
        if (sender instanceof Player == false)
        {
            sender.sendMessage("This command can only be used by players");
            return true;
        }

        ((Player) sender).kickPlayer(ChatColor.GREEN + "Client closed the connection.");
        return true;

    }
}