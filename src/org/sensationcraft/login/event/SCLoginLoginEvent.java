package org.sensationcraft.login.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

/**
 *
 * @author DarkSeraphim
 */
public class SCLoginLoginEvent extends SCLoginEvent
{

	private static final HandlerList handlers = new HandlerList();
        
        private final Player player;

        public SCLoginLoginEvent(String name, Player player)
        {
            super(name);
            this.player = player;
        }
        
        public Player getPlayer()
        {
                return this.player;
        }
        
	public HandlerList getHandlers()
	{
		return SCLoginLoginEvent.handlers;
	}

	public static HandlerList getHandlerList()
	{
		return SCLoginLoginEvent.handlers;
	}
}
