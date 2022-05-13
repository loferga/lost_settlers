package fr.loferga.lost_settlers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import fr.loferga.lost_settlers.game.Start;
import fr.loferga.lost_settlers.game.EndGame;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.RestoreMap;
import fr.loferga.lost_settlers.teams.LSTeam;

public class Main extends JavaPlugin{
	
	private ProtocolManager protocolManager;
	
	// Dynamic
	public static Map<Integer, List<Player>> glow = new HashMap<>();
	public static World map = null;
	
	public void onEnable() {
		
		saveDefaultConfig();
		
		new MapMngr();
		Recipes.createRecipes(this);
		getCommand("lsteam").setExecutor(new LSTeam());
		getCommand("start").setExecutor(new Start());
		getCommand("end").setExecutor(new EndGame());
		getCommand("restoremap").setExecutor(new RestoreMap());
		getServer().getPluginManager().registerEvents(new Listeners(), this);
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA) {
					@Override
				    public void onPacketSending(PacketEvent e) {
				    	WrappedWatchableObject wwo = e.getPacket().getWatchableCollectionModifier().read(0).get(0);
				    	if (wwo.getIndex() == 0 && (byte) wwo.getRawValue() == 0b01000000) {
				    		Integer eID = e.getPacket().getIntegers().read(0);
				    		if (glow.keySet().contains(eID)) {
				    			int i = glow.get(eID).indexOf(e.getPlayer());
				    			if (i == -1 )
				    				e.setCancelled(true);
				    			else
				    				if (glow.get(eID).size() > 1)
				    					glow.get(eID).remove(i);
				    				else
				    					glow.remove(eID);
				    		}
				    	}
					}
				 });
		
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] ===============================");
		console.sendMessage("[LostSettlers]   " + ChatColor.AQUA + "Plugin " + ChatColor.GOLD + "Lost Settlers" + ChatColor.DARK_AQUA + " active");
		console.sendMessage("[LostSettlers] ===============================");
	}
	
	public void onDisable() {
		if (map != null)
			Bukkit.unloadWorld(map, false);
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "Plugin Disabled");
    }
	
}