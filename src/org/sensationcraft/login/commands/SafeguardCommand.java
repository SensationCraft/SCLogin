package org.sensationcraft.login.commands;

import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

public class SafeguardCommand extends SCLoginMasterCommand
{

    private SCLogin plugin;
    
    private final Set<String> enabled = Sets.newHashSet("enable", "1", "true", "yes");
    private final Set<String> disabled = Sets.newHashSet("disable", "0", "false", "no");

    public SafeguardCommand(SCLogin plugin)
    {
        super("safeguard");
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
        
        Player player = (Player) sender;
        
        if(args.length == 0)
        {
            player.sendMessage(Messages.SAFEGUARD_INFO.getMessage());
            return true;
        }
        
        args[0] = args[0].toLowerCase();
        
        if(args.length > 1 || (!this.enabled.contains(args[0]) && !this.disabled.contains(args[0])))
        {
            player.sendMessage(Messages.INVALID_SYNTAX.getMessage()+this.usage);
            return true;
        }
        
        boolean activate = this.enabled.contains(args[0]);
        
        if(this.plugin.getPlayerManager().setLockedToIp(player.getName(), activate))
        {
            player.sendMessage(activate ? Messages.SAFEGUARD_ENABLED.getMessage() : Messages.SAFEGUARD_DISABLED.getMessage());
        }
        else
        {
            player.sendMessage(activate ? Messages.SAFEGUARD_ALREADY_ENABLED.getMessage() : Messages.SAFEGUARD_ALREADY_DISABLED.getMessage());
        }
        
        return true;
    }
}