package org.sensationcraft.login.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public abstract class SCLoginMasterCommand
{

    final String usage;

    SCLoginMasterCommand(String command)
    {
        this.usage = Bukkit.getPluginCommand(command).getUsage();
    }

    public abstract boolean execute(CommandSender sender, String[] args);
}
