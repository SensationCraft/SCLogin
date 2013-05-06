package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.PasswordManager;
import org.sensationcraft.login.SCLogin;

public class ChangePasswordCommand extends SCLoginMasterCommand
{

    private SCLogin plugin;
    
    public ChangePasswordCommand(SCLogin plugin)
    {
        this.plugin = plugin;
    }
    
    
    @Override
    public boolean execute(CommandSender sender, final String[] args)
    {
        if(sender instanceof Player == false)
        {
            sender.sendMessage("This command can only be used by players");
            return true;
        }
        
        if(args.length != 3)
        {
            sender.sendMessage(ChatColor.RED+"Incorrect syntax! Correct usage: /changepassword <old password> <new password> <confirm new password>");
            return true;
        }
        
        if(!args[1].equals(args[2]))
        {
            sender.sendMessage(ChatColor.RED+"Your entered password and the confirmation password don't seem to match.");
            return true;
        }
        
        final Player player = (Player)sender;
        
        new BukkitRunnable()
        {
                @Override
                public void run()
                {
                    String name = player.getName();
                    
                    PasswordManager pwmanager = ChangePasswordCommand.this.plugin.getPasswordManager();
                    
                    if(pwmanager.checkPassword(name, args[0], player.getAddress().getAddress().getHostAddress()))
                    {
                        pwmanager.changePassword(name, args[1]);
                        player.sendMessage(ChatColor.GREEN+"Changed the password.");
                    }
                    
                }
        }.runTaskAsynchronously(this.plugin);
        return true;
    }

}
