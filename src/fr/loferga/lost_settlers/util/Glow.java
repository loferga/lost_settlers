package fr.loferga.lost_settlers.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class Glow {
	
	private static ProtocolManager protocolManager;
	
	public static Map<Integer, Set<Player>> glow = new HashMap<>();
	
	public static void addGlow(Integer i, Set<Player> pset) {
		glow.put(i, pset);
	}
	
	public static void addPacketListener(Plugin plg) {

		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(plg, PacketType.Play.Server.ENTITY_METADATA) {
					@Override
				    public void onPacketSending(PacketEvent e) {
				    	WrappedWatchableObject wwo = e.getPacket().getWatchableCollectionModifier().read(0).get(0);
				    	if (wwo.getIndex() == 0 && (byte) wwo.getRawValue() == 0b01000000) {
				    		Integer eID = e.getPacket().getIntegers().read(0);
				    		if (glow.keySet().contains(eID)) {
				    			if (!glow.get(eID).contains(e.getPlayer()))
				    				e.setCancelled(true);
				    			else
				    				if (glow.get(eID).size() > 1)
				    					glow.get(eID).remove(e.getPlayer());
				    				else
				    					glow.remove(eID);
				    		}
				    	}
					}
				 });
		
	}
	
}