package fr.loferga.lost_settlers.game;

import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.MapMngr;

public class GameLaunch extends BukkitRunnable {
	
	private Game game;
	private Map<Location, Material> map;
	
	public GameLaunch(Game game, Map<Location, Material> map, Plugin plugin) {
		this.game = game;
		this.map = map;
		this.runTaskTimer(plugin, 0L, 20L);
	}
	
	private int chrono = 5;
	private String codes = "b46eaa";

	@Override
	public void run() {
		for (Player p : game.getPlayers()) {
			p.sendTitle(Func.format("&" + codes.charAt(chrono) + chrono), null, 10, 10, 20);
			p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
		}
		if (chrono <= 0) {
			launchGame();
			cancel();
		}
		chrono--;
	}
	
	private void launchGame() {
		game.runTaskTimer(Main.getPlugin(Main.class), 0L, 1L);
		MapMngr.clearBeacons(map);
		for (Player p : game.getPlayers()) {
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

}
