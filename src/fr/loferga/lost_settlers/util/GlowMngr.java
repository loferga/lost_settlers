package fr.loferga.lost_settlers.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import fr.loferga.lost_settlers.Main;

public class GlowMngr {
	
	private GlowMngr() {/*fonction holder class, it should never be instantiated*/}
	
	private static Map<Integer, Set<Player>> glow = new HashMap<>();
	
	public static void glowFor(LivingEntity ent, Set<Player> p, int duration) {
		glow.put(ent.getEntityId(), p);
		ent.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, duration, 0, false, false));
	}
	
	public static void addPacketListener() {
		
		ProtocolLibrary.getProtocolManager().addPacketListener(
				new PacketAdapter(Main.PLG, PacketType.Play.Server.ENTITY_METADATA) {
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