package fr.loferga.lost_settlers.game;

import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.util.Func;

public class GameLaunch extends BukkitRunnable {
	
	private Game game;
	private Map<Location, Material> map;
	
	public GameLaunch(Game game, Map<Location, Material> map, Plugin plugin) {
		this.game = game;
		this.map = map;
		this.runTaskTimer(plugin, 0L, 20L);
	}
	
	private static final char NL = '@';
	
	private int chrono = 10;
	private String codes = "b46e"+NL+"a"+NL+NL+NL+NL+NL;

	@Override
	public void run() {
		char c = codes.charAt(chrono);
		if (c != NL)
			for (Player p : game.getPlayers()) {
				p.sendTitle(Func.format("&" + c + chrono), null, 10, 10, 20);
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			}
		if (chrono-- <= 0) {
			launchGame();
			cancel();
		}
	}
	
	private void launchGame() {
		game.runTaskTimer(Main.PLG, 0L, 1L);
		MapMngr.clearMap(map);
		for (Player p : game.getPlayers()) {
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

}
