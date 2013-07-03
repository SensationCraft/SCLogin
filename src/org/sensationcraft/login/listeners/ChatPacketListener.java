package org.sensationcraft.login.listeners;

import org.bukkit.entity.Player;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

/**
 *
 * @author s129977
 */
public class ChatPacketListener extends PacketAdapter
{

	public ChatPacketListener(final SCLogin plugin)
	{
		super(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, Packets.Server.CHAT);

	}

	@Override
	public void onPacketSending(final PacketEvent event)
	{
		if (event.getPacketID() == Packets.Server.CHAT)
		{
			final PacketContainer packet = event.getPacket();
			final String message = packet.getStrings().read(0);
			final Player player = event.getPlayer();
			if(Messages.isSCLoginMessage(message)) return;
			if(!((SCLogin)this.getPlugin()).getPlayerManager().isLoggedIn(player.getName()))
			{
				event.setCancelled(true);
			}
		}
	}
}
