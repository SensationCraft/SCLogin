package org.sensationcraft.login.event;

import org.bukkit.event.HandlerList;

/**
 *
 * @author DarkSeraphim
 */
public class SCLoginRegisterEvent extends SCLoginEvent
{

	private final boolean isNew;

	private static final HandlerList handlers = new HandlerList();

	public SCLoginRegisterEvent(final String name, final boolean isNew)
	{
		super(name);
		this.isNew = isNew;
	}

	public boolean isNewPlayer()
	{
		return this.isNew;
	}

	public HandlerList getHandlers()
	{
		return SCLoginRegisterEvent.handlers;
	}

	public static HandlerList getHandlerList()
	{
		return SCLoginRegisterEvent.handlers;
	}
}
