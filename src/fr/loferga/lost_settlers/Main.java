package fr.loferga.lost_settlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fr.loferga.lost_settlers.game.Start;
import fr.loferga.lost_settlers.gui.GUIMngr;
import fr.loferga.lost_settlers.game.All;
import fr.loferga.lost_settlers.game.End;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.ClearOres;
import fr.loferga.lost_settlers.map.CloseWorld;
import fr.loferga.lost_settlers.map.Lobby;
import fr.loferga.lost_settlers.map.Warp;
import fr.loferga.lost_settlers.skills.SkillCommand;
import fr.loferga.lost_settlers.skills.SkillListeners;
import fr.loferga.lost_settlers.teams.SelectTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.Glow;

public class Main extends JavaPlugin{
	
	public static final ItemStack SELECTOR = new ItemStack(Material.COMPASS, 1);
	
	public void onEnable() {
		
		saveDefaultConfig();
		
		new MapMngr();
		new GUIMngr();
		
		MapMngr.HUB.setPVP(false);
		
		// in case of a reload, players that already are in hub need to be initialized
		for (Player p : MapMngr.HUB.getPlayers()) {GUIMngr.giveSelector(p); TeamMngr.join(p, TeamMngr.NULL);}
		
		Recipes.createRecipes(this);
		
		getCommand("lobby").setExecutor(new Lobby());
		getCommand("all").setExecutor(new All());
		getCommand("lsteam").setExecutor(new SelectTeam());
		getCommand("skill").setExecutor(new SkillCommand());
		getCommand("start").setExecutor(new Start());
		getCommand("end").setExecutor(new End());
		getCommand("warp").setExecutor(new Warp());
		getCommand("close").setExecutor(new CloseWorld());
		getCommand("clearores").setExecutor(new ClearOres());
		getServer().getPluginManager().registerEvents(new Listeners(), this);
		getServer().getPluginManager().registerEvents(new SkillListeners(), this);
		
		Glow.addPacketListener(this);
		
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] ===============================");
		console.sendMessage("[LostSettlers]   " + ChatColor.AQUA + "Plugin " + ChatColor.GOLD + "Lost Settlers" + ChatColor.DARK_AQUA + " active");
		console.sendMessage("[LostSettlers] ===============================");
	}
	
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers())
			if (MapMngr.isMap(p.getWorld()))
				MapMngr.spawnTeleport(p);
		TeamMngr.removeAllTeams();
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "Plugin Disabled");
    }
	
}