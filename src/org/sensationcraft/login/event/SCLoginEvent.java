package org.sensationcraft.login.event;

import org.bukkit.event.Event;

/**
 *
 * @author DarkSeraphim
 */
public abstract class SCLoginEvent extends Event
{
    
    private final String name;
    
    public SCLoginEvent(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }

}
