package org.sensationcraft.login.commands;

import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.SCLogin;

public class SCLoginCommand extends SCLoginMasterCommand
{

	private SCLogin plugin;
        
        private final String description;
        
        private final Set<String> forbidden = Sets.newHashSet("123456", "password", "<password>");
	
	public SCLoginCommand(SCLogin plugin)
        {
		this.plugin = plugin;
                StringBuilder desc = new StringBuilder("----- SCLogin -----");
                for(Subcommand sc : Subcommand.values())
                {
                    desc.append("/sclogin");
                    if(!sc.getCommand().isEmpty()) desc.append(sc.getCommand()).append(" ");
                    desc.append(" - ").append(sc.getDescription()).append("\n");
                }
                desc.append("-------------------");
                this.description = desc.toString();
	}
	
	@Override
	public boolean execute(final CommandSender sender, final String[] args) 
        {
	            
                if(!sender.hasPermission(("sclogin.mod")))
                {
                    sender.sendMessage("Unknown command. Type \"help\" for help.");
                    return true;
                }
                if(args.length == 0)
                {
                    sender.sendMessage(this.description);
                    return true;
                }
            
                final Subcommand sub = Subcommand.getSubcommand(args[0]);
                
                if(args.length < 2 || ((sub == Subcommand.CHANGEPW || sub ==  Subcommand.COUNT) && args.length < 3))
                {
                    sender.sendMessage("Invalid arguments!");
                    return true;
                }
                
                if(sub == Subcommand.CHANGEPW)
                {
                    if(!args[1].equals(args[2]))
                    {
                        sender.sendMessage(ChatColor.RED+"Your entered password and the confirmation password don't seem to match.");
                        return true;
                    }

                    if(args[0].length() < 6)
                    {
                            sender.sendMessage(ChatColor.RED+"Your entered password is too short. At least 6 characters are required.");
                            return true;
                    }

                    if(this.forbidden.contains(args[0].toLowerCase()))
                    {
                            sender.sendMessage(ChatColor.RED+"Please pick another password.");
                            return true;
                    }
                }
                
                if(!sender.hasPermission(sub.getPermission()))
                {
                    sender.sendMessage(ChatColor.RED+"Insufficient permissions!");
                    return true;
                }
                                
		new BukkitRunnable(){

			@Override
			public void run() 
                        {
                            switch(sub)
                            {
                                // Add cases here
                                case UNREGISTER:
                                    SCLoginCommand.this.plugin.getPlayerManager().unregister(args[1]);
                                    break;
                                case LOCK:
                                    if(SCLoginCommand.this.plugin.getPlayerManager().isLocked(args[1]))
                                    {
                                        sender.sendMessage(ChatColor.RED+"That account is already locked");
                                        break;
                                    }
                                    SCLoginCommand.this.plugin.getPlayerManager().setLocked(args[1], true);
                                    sender.sendMessage(ChatColor.GREEN+String.format("Account '%s' is now locked", args[1]));
                                    break;
                                case UNLOCK:
                                    if(!SCLoginCommand.this.plugin.getPlayerManager().isLocked(args[1]))
                                    {
                                        sender.sendMessage(ChatColor.RED+"That account is already unlocked");
                                        break;
                                    }
                                    SCLoginCommand.this.plugin.getPlayerManager().setLocked(args[1], false);
                                    sender.sendMessage(ChatColor.GREEN+String.format("Account '%s' is now unlocked", args[1]));
                                    break;
                                case COUNT:
                                    String count = SCLoginCommand.this.plugin.getPlayerManager().getCount(args[2]);
                                    if(count == null)
                                    {
                                        sender.sendMessage("Incorrect usage: /sclogin count active|locked");
                                        break;
                                    }
                                    sender.sendMessage(ChatColor.GREEN+String.format("There are %s %s accounts", count, (args[2].startsWith("a") ? "active" : "locked")));
                                    break;
                                case CHANGEPW:
                                    
                                    SCLoginCommand.this.plugin.getPasswordManager().changePassword(args[1], args[2]);
                                    sender.sendMessage(ChatColor.GREEN+"Password was changed.");
                                    break;
                                case PROFILE:
                                    String profile = SCLoginCommand.this.plugin.getPlayerManager().getProfile(args[1]);
                                    sender.sendMessage(profile);
                                    break;
                                default:
                                    sender.sendMessage(ChatColor.RED+"invalid command. Use /sclogin for a full list of commands");
                                    break;
                            }
			}
			
		}.runTaskAsynchronously(this.plugin);
		return true;
	}
        
        private enum Subcommand
        {
            // and subcommands here!
            NONE("", "displays this help menu", "sclogin.mod"),
            UNREGISTER("unregister", "unregisters an account, so that it is free to be registered once again", "sclogin.admin.unregister"),
            LOCK("lock", "locks an account, so that it cannot be used", "sclogin.admin.lock"),
            UNLOCK("unlock", "unlocks an account, so that it can be used again", "sclogin.admin.lock"),
            COUNT("count", "displays the statistics of SCLogin", "sclogin.mod.count"),
            CHANGEPW("changepassword", "changes the password of a player", "sclogin.admin.changepw"),
            PROFILE("profile", "displays player info", "sclogin.mod.profile");
           
            private final String command;
            
            private final String desc;
            
            private final String permission;
            
            Subcommand(String command, String description, String permission)
            {
                this.command = command;
                this.desc = description;
                this.permission = permission;
            }
            
            public String getCommand()
            {
                return this.command;
            }
            
            public String getDescription()
            {
                return this.desc;
            }
            
            public String getPermission()
            {
                return this.permission;
            }
            
            public static Subcommand getSubcommand(String cmd)
            {
                for(Subcommand sc : values())
                {
                    if(sc.getCommand().equalsIgnoreCase(cmd)) return sc;
                }
                return NONE;
            }
        }

}
