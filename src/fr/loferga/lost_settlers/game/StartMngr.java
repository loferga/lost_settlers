package fr.loferga.lost_settlers.game;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.camps.CampMngr;
import fr.loferga.lost_settlers.particles.ParticleMngr;
import fr.loferga.lost_settlers.tasks.GameLaunch;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class StartMngr {
	
	
	/* start:
	 * worldBorders to maxDist + 150
	 * time set to 0
	 * 
	 * initiate scoreboard for everyone wich include:
	 * 		timer (with time until combat)
	 * 		current team state
	 * 		other team members
	 * 
	 * for each team:
	 * 		initiate flags
	 * 		initiate beacon beam
	 * 		for each players in team:
	 * 			teleport around the flag
	 * 			foodlevel to 30
	 * 			health to 20.0
	 * 			exp to 0.0f
	 * 			clear inv
	 * difficulty to EASY
	 * start game tasks
	 */
	
	public static void start() {
		// MAP
		FileConfiguration cfg = Main.getPlugin(Main.class).getConfig();
		String cMap = cfg.getString("current_map");
		if (Main.map == null || Main.map.getName() != cMap) {
			if (Main.map == null) {
				Bukkit.unloadWorld(Main.map, false);
			}
			Bukkit.broadcastMessage(Func.format("&cChargement de la carte ...&r, \'" + cMap + '\''));
			Main.map = new WorldCreator("-LS-" + cMap).createWorld();
			MapMngr.buildMapVars(cfg.getConfigurationSection("maps.".concat(cMap)));
		}
		System.out.println(Main.map);
		CampMngr.initFlags();
		// PLAYERS
		for (Team team : TeamMngr.get()) {
			team.setOption(Option.COLLISION_RULE, OptionStatus.ALWAYS);
			Location tpLoc = CampMngr.getTeamCamps(team).get(0).getLoc();
			for (Player p : TeamMngr.getPlayers(team)) {
				MapMngr.teleportAround(tpLoc, p);
				p.setGameMode(GameMode.ADVENTURE);
				p.setFoodLevel(30);
				p.setHealth(20.0);
				p.setExp(0.0f);
				p.setLevel(0);
				p.getInventory().clear();
			}
			// beacon beam effect
		}
		for (Entity i : Main.map.getEntitiesByClasses(Item.class))
			i.remove();
		Main.map.setDifficulty(Difficulty.EASY);
		setWorldBorder();
		Main.map.setTime(0);
		ParticleMngr.disableTrails();
		GameLaunch.start(Main.getPlugin(Main.class));
	}
	
	private static void setWorldBorder() {
		Location loc = MapMngr.getMapCenter();
		double maxDist = MapMngr.getMaxDist(loc);
		WorldBorder wb = Main.map.getWorldBorder();
		wb.setCenter(loc);
		wb.setSize((int) (maxDist + 150));
	}

}
