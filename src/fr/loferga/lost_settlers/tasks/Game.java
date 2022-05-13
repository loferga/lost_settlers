package fr.loferga.lost_settlers.tasks;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Game extends BukkitRunnable {
	
	private static int pvp_time;
	private static int half;
	private static int fifth;
	private static int fifteenth;
	private static boolean active = false;
	private static boolean pvp = false;
	private static long startT;
	
	private static void initT() {
		pvp_time = Main.getPlugin(Main.class).getConfig().getInt("pvp_time");
		half = pvp_time - pvp_time/2;
		fifth = pvp_time - pvp_time/5;
		fifteenth = pvp_time - pvp_time/15;
	}
	
	public static void start(Plugin plugin) {
		active = true;
		initT();
		startT = System.currentTimeMillis();
		new Game().runTaskTimer(plugin, 0L, 20L);
	}
	
	public static void stop() {
		active = false;
		pvp = false;
		chrono = 0;
	}
	
	public static boolean active() {
		return active || GameLaunch.active();
	}
	
	public static boolean pvp() {
		return pvp;
	}
	
	public static long getStartTime() {
		return startT;
	}
	
	private static int chrono = 0;

	@Override
	public void run() {
		if (active) {
			if (chrono == half) {
				String[] rt = Func.toReadableTime((long) (Math.abs(half - pvp_time)) * 1000);
				Bukkit.broadcastMessage(Func.format("&aIl reste &3" + rt[0] + rt[1] + rt[2] + "&a avant le debut des captures"));
			}
			if (chrono == fifth) {
				String[] rt = Func.toReadableTime((long) (Math.abs(fifth - pvp_time)) * 1000);
				Bukkit.broadcastMessage(Func.format("&eIl reste &3" + rt[0] + rt[1] + rt[2] + "&e avant le debut des captures"));
			}
			if (chrono == fifteenth) {
				String[] rt = Func.toReadableTime((long) (Math.abs(fifteenth - pvp_time)) * 1000);
				Bukkit.broadcastMessage(Func.format("&6Il reste &3" + rt[0] + rt[1] + rt[2] + "&6 avant le debut des captures"));
			}
			if (chrono == pvp_time) {
				Bukkit.broadcastMessage(Func.format("&4Captures et Combat Actives"));
				for (Player p : TeamMngr.getTeamedPlayers())
					p.playSound(Main.map.getSpawnLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, SoundCategory.MASTER, 400.0f, 1.0f);
				pvp = true;
			}
			chrono++;
		} else cancel();
	}
	
}
