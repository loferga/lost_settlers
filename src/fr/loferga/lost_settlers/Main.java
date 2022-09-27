package fr.loferga.lost_settlers;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldCreator;
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
import fr.loferga.lost_settlers.gui.GUIMngr;
import fr.loferga.lost_settlers.game.All;
import fr.loferga.lost_settlers.game.End;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.ClearOres;
import fr.loferga.lost_settlers.map.CloseWorld;
import fr.loferga.lost_settlers.map.Lobby;
import fr.loferga.lost_settlers.map.Warp;
import fr.loferga.lost_settlers.skills.SkillListeners;
import fr.loferga.lost_settlers.teams.SelectTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Main extends JavaPlugin{
	
	private static ProtocolManager protocolManager;
	
	public static Map<Integer, Set<Player>> glow = new HashMap<>();
	public static World hub;
	
	public void onEnable() {
		
		saveDefaultConfig();
		
		hub = new WorldCreator(Main.getPlugin(Main.class).getConfig().getString("maps.spawn.worldname")).createWorld();
		hub.setPVP(false);
		
		new MapMngr();
		new GUIMngr();
		Recipes.createRecipes(this);
		
		getCommand("lobby").setExecutor(new Lobby());
		getCommand("all").setExecutor(new All());
		getCommand("lsteam").setExecutor(new SelectTeam());
		getCommand("start").setExecutor(new Start());
		getCommand("end").setExecutor(new End());
		getCommand("warp").setExecutor(new Warp());
		getCommand("close").setExecutor(new CloseWorld());
		getCommand("clearores").setExecutor(new ClearOres());
		getServer().getPluginManager().registerEvents(new Listeners(), this);
		getServer().getPluginManager().registerEvents(new SkillListeners(), this);
		
		protocolManager = ProtocolLibrary.getProtocolManager();
		protocolManager.addPacketListener(
				new PacketAdapter(this, PacketType.Play.Server.ENTITY_METADATA) {
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
		
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] ===============================");
		console.sendMessage("[LostSettlers]   " + ChatColor.AQUA + "Plugin " + ChatColor.GOLD + "Lost Settlers" + ChatColor.DARK_AQUA + " active");
		console.sendMessage("[LostSettlers] ===============================");
	}
	
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (p.getWorld() != hub)
				TeamMngr.remove(p);
				MapMngr.spawnTeleport(p);
			}
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "Plugin Disabled");
    }
	
}