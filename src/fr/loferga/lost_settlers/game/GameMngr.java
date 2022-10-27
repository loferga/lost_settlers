package fr.loferga.lost_settlers.game;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpawnCategory;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.MapSettings;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.skills.SkillListeners;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.Func;

public class GameMngr {
	
	private GameMngr() {/*fonction holder class, it should never be instantiated*/}
	
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
		Main.plg().reloadConfig();
		for (Player p : MapMngr.HUB.getPlayers()) p.sendMessage(Func.format('&' + Main.MSG_ANNOUNCE + "Chargement de la carte &r" + wn));
		World world = MapMngr.newWorld(wn);
		MapSettings ms = MapMngr.getMapSettings(world);
		if (!ms.canHostGame()) return;
		
		world.setDifficulty(Difficulty.EASY);
		world.setTime(0);
//		world.setSpawnFlags(true, true);
		world.setSpawnLimit(SpawnCategory.WATER_ANIMAL, 8);
		world.setSpawnLimit(SpawnCategory.MONSTER, 20);
		Location center = MapMngr.getMapCenter(world, ms);
		if (center != world.getSpawnLocation()) world.setSpawnLocation(center);
		// GAME
		Game game = new Game(world, TeamMngr.teamedPlayers(MapMngr.HUB));
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
		new GameLaunch(game, MapMngr.setMap(ms), Main.plg());
	}
	
	public static void stop(Game game, LSTeam winner) {
		long gameTime = System.currentTimeMillis() - game.getStartTime();
		if (winner != null) game.broadcastPlayers(Func.format(Main.MSG_ANNOUNCE + "L'equipe des " + winner.getName()
				+ Main.MSG_ANNOUNCE + " a remportee la victoire!"));
		String[] rt = Func.toReadableTime(gameTime);
		game.broadcastPlayers(Func.format(Main.MSG_ANNOUNCE + "La partie s\'est terminee en &3"
				+ rt[0] + rt[1] + rt[2] + Main.MSG_ANNOUNCE + "."));
		game.clearFlags();
		for (Camp c : game.getMapSettings().camps)
			c.killZoneEffect();
		World gw = game.getWorld();
		gw.getWorldBorder().setSize(Double.MAX_VALUE);
		gw.setDifficulty(Difficulty.PEACEFUL);
//		gw.setSpawnFlags(false, false);
		for (Player p : game.getPlayers())
			p.setGameMode(GameMode.ADVENTURE);
		remove(game);
		game.stop();
		
	}

}