package fr.loferga.lost_settlers;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
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
import fr.loferga.lost_settlers.rules.CombatTracker;
import fr.loferga.lost_settlers.rules.Wounded;
import fr.loferga.lost_settlers.skills.SkillCommand;
import fr.loferga.lost_settlers.skills.SkillListeners;
import fr.loferga.lost_settlers.teams.SelectTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.GlowMngr;

public class Main extends JavaPlugin{
	
//	private static final Plugin PLUGIN = JavaPlugin.getPlugin(Main.class);
	
	public static Plugin plg() {
		return JavaPlugin.getPlugin(Main.class);
	}
	
	public static final String LOG_PREFIX = "[LostSettlers] ";
	public static final String MSG_WARNING = "c";
	public static final String MSG_ANNOUNCE = "e";
	public static final String MSG_PERSONNAL = "6";
	public static final String MSG_ERROR = "4";
	public static final String MSG_DONE = "a";
	
	@Override
	public void onEnable() {
		
		saveDefaultConfig();
		
//		new MapMngr();
//		new GUIMngr();
		
		if (MapMngr.AUTO_LOAD)
			for (String wn : getConfig().getConfigurationSection("maps").getKeys(false))
				if (!wn.equals("lobby"))
					MapMngr.newWorld(wn);
		
		MapMngr.HUB.setPVP(false);
		MapMngr.HUB.setGameRule(GameRule.KEEP_INVENTORY, true);
		
		// in case of a reload, players that already are in hub need to be initialized
		for (Player p : MapMngr.HUB.getPlayers()) {GUIMngr.giveSelector(p); TeamMngr.join(p, TeamMngr.NULL);}
		
		Recipes.createRecipes();
		
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
		getServer().getPluginManager().registerEvents(Wounded.getInstance(), this);
		getServer().getPluginManager().registerEvents(CombatTracker.getInstance(), this);
		
		GlowMngr.addPacketListener();
		
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] ===============================");
		console.sendMessage("[LostSettlers]   " + ChatColor.AQUA + "Plugin " + ChatColor.GOLD + "Lost Settlers" + ChatColor.DARK_AQUA + " active");
		console.sendMessage("[LostSettlers] ===============================");
	}
	
	@Override
	public void onDisable() {
		for (Player p : Bukkit.getOnlinePlayers())
			if (MapMngr.isMap(p.getWorld()))
				MapMngr.spawnTeleport(p);
		TeamMngr.removeAllTeams();
		ConsoleCommandSender console = getServer().getConsoleSender();
		console.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "Plugin Disabled");
    }
	
}