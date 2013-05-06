package org.sensationcraft.login.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;
import org.sensationcraft.login.SCLogin;

public class SCLoginCommand extends SCLoginMasterCommand
{

	private SCLogin plugin;
	
	public SCLoginCommand(SCLogin plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean execute(final CommandSender sender, final String[] args) {
		
                if(!sender.hasPermission(("can.I.see.this.at.all?")))
                {
                    sender.sendMessage("Unknown command. Type \"help\" for help.");
                    return true;
                }
                if(args.length == 0)
                {
                    sender.sendMessage("Help message with the commands");
                    return true;
                }
            
		new BukkitRunnable(){

			@Override
			public void run() 
                        {
                            switch(Subcommand.getSubcommand(args[0]))
                            {
                                // Add cases here
                                default:
                                    sender.sendMessage(ChatColor.RED+"invalid command. Use /sclogin for a full list of commands");
                                    break;
                            }
			}
			
		}.runTaskAsynchronously(this.plugin);
		return false;
	}
        
        private enum Subcommand
        {
            // and subcommands here!
            NONE("");
           
            private final String command;
            
            Subcommand(String command)
            {
                this.command = command;
            }
            
            public String getCommand()
            {
                return this.command;
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
