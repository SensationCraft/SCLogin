package org.sensationcraft.login.listeners;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import org.bukkit.entity.Player;
import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.messages.Messages;

/**
 *
 * @author s129977
 */
public class ChatPacketListener extends PacketAdapter
{

    public ChatPacketListener(SCLogin plugin)
    {
        super(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, Packets.Server.CHAT);

    }

    @Override
    public void onPacketReceiving(PacketEvent event)
    {
        if (event.getPacketID() == Packets.Server.CHAT)
        {
            PacketContainer packet = event.getPacket();
            String message = packet.getStrings().read(0);
            Player player = event.getPlayer();
            if(Messages.isSCLoginMessage(message)) return;
            if(!((SCLogin)getPlugin()).getPlayerManager().isLoggedIn(player.getName()))
            {
                event.setCancelled(true);
            }
        }
    }
}
