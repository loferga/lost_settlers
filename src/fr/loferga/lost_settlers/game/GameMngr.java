package fr.loferga.lost_settlers.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.MapSettings;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.skills.SkillListeners;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class GameMngr {
	
	private static List<Game> games = new ArrayList<>();
	
	public static boolean noGames() {
		return games.isEmpty();
	}
	
	public static void add(Game game) {
		games.add(game);
	}
	
	public static void remove(Game game) {
		games.remove(game);
	}
	
	public static Game getGame(World world) {
		for (Game game : games)
			if (game.getWorld() == world)
				return game;
		return null;
	}
	
	public static Game gameIn(Player p) {
		for (Game g : games) 
			if (g.getPlayers().contains(p))
				return g;
		return null;
	}
	
	/* start:
	 * worldBorders to maxDist + 150
	 * time set to 0
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
	
	public static void start(String wn) {
		// MAP
		Main.getPlugin(Main.class).reloadConfig();
		for (Player p : Main.hub.getPlayers()) p.sendMessage(Func.format("Chargement de la carte &e" + wn));
		World world = MapMngr.newWorld(wn);
		MapSettings ms = new MapSettings(world, Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps." + wn));
		MapMngr.add(ms);
		world.setDifficulty(Difficulty.EASY);
		world.setTime(0);
		world.setSpawnFlags(true, true);
		world.setWaterAnimalSpawnLimit(8);
		world.setMonsterSpawnLimit(20);
		Location center = MapMngr.getMapCenter(world, ms);
		if (center != world.getSpawnLocation()) world.setSpawnLocation(center);
		// GAME
		Game game = new Game(world, TeamMngr.teamedPlayers(Main.hub));
		game.buildMap();
		add(game);
		// PLAYERS
		for (LSTeam team : TeamMngr.get()) {
			Camp tCamp = game.getTeamCamps(team).get(0);
			for (Player p : team.getPlayers()) {
				MapMngr.campTeleport(p, tCamp);
				p.setBedSpawnLocation(p.getLocation());
				p.setGameMode(GameMode.ADVENTURE);
				p.setFoodLevel(30);
				p.setHealth(20.0);
				p.setExp(0.0f);
				p.setLevel(0);
				p.getInventory().clear();
			}
		}
		SkillListeners.giveEquipment(game);
		for (Entity i : world.getEntitiesByClasses(Item.class))
			i.remove();
		new GameLaunch(game, MapMngr.setMap(ms), Main.getPlugin(Main.class));
	}
	
	public static void stop(Game game, LSTeam winner) {
		long gameTime = System.currentTimeMillis() - game.getStartTime();
		if (winner != null) Bukkit.broadcastMessage(Func.format("&eL'equipe des " + winner.getName() + "&e a remportee la victoire!"));
		String[] rt = Func.toReadableTime(gameTime);
		Bukkit.broadcastMessage(Func.format("&eLa partie s\'est terminee en &3" + rt[0] + rt[1] + rt[2] + "&e."));
		game.clearFlags();
		game.unfreezeChamber();
		World gw = game.getWorld();
		gw.getWorldBorder().setSize(Double.MAX_VALUE);
		gw.setDifficulty(Difficulty.PEACEFUL);
		gw.setSpawnFlags(false, false);
		for (Player p : game.getPlayers())
			p.setGameMode(GameMode.ADVENTURE);
		remove(game);
		game.stop();
	}

}