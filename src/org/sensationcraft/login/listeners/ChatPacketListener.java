package org.sensationcraft.login.listeners;

import java.util.Arrays;
import java.util.List;

import org.sensationcraft.login.SCLogin;
import org.sensationcraft.login.api.SCLoginAPI;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class ChatPacketListener extends PacketAdapter{

	private static List<String> allowedMessages = Arrays.asList(new String[] {"Hi", "Penis"}); //TODO addd
	
	public ChatPacketListener(SCLogin plugin) {
		super(plugin, ConnectionSide.SERVER_SIDE, ListenerPriority.HIGHEST, Packets.Server.CHAT);
	}
	
    @Override
    public void onPacketSending(PacketEvent event) {
    	if(!SCLoginAPI.isAuthenticated(event.getPlayer().getName()))
    		if(!allowedMessages.contains(event.getPacket().getStrings().read(0)))
    			event.setCancelled(true);
    	
    }
	
}
