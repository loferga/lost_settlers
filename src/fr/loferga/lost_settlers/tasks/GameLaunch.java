package fr.loferga.lost_settlers.tasks;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class GameLaunch extends BukkitRunnable {
	
	private static boolean active = false;
	
	public static void start(Plugin plugin) {
		active = true;
		new GameLaunch().runTaskTimer(plugin, 0L, 20L);
	}
	
	private static int chrono = 3;
	private static String codes = "ae64";

	@Override
	public void run() {
		if (active) {
			for (Player p : TeamMngr.getTeamedPlayers()) {
				p.sendTitle(Func.format("&" + codes.charAt(chrono) + chrono), null, 10, 10, 20);
				p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
			}
			if (chrono <= 0) {
				launchGame();
				active = false;
				chrono = 4;
			}
			chrono--;
		} else cancel();
	}
	
	private static void launchGame() {
		Plugin plugin = Main.getPlugin(Main.class);
		Game.start(plugin);
		ComeBack.start(plugin);
		Conquest.start(plugin);
		DogAnger.start(plugin);
		NaturalRegen.start(plugin);
		RespawnCooldown.start(plugin);
		for (Player p : TeamMngr.getTeamedPlayers()) {
			p.setGameMode(GameMode.SURVIVAL);
		}
	}

}
