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
	
	private static final int PVP_T = Main.getPlugin(Main.class).getConfig().getInt("pvp_time");
	private static final int HALF = PVP_T - PVP_T/2;
	private static final int FIFTH = PVP_T - PVP_T/5;
	private static final int FIFTEENTH = PVP_T - PVP_T/15;
	private static boolean active = false;
	private static boolean pvp = false;
	private static long startT;
	
	public static void start(Plugin plugin) {
		active = true;
		startT = System.currentTimeMillis();
		new Game().runTaskTimer(plugin, 0L, 20L);
	}
	
	public static void stop() {
		active = false;
		pvp = false;
		chrono = 0;
	}
	
	public static boolean active() {
		return active;
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
			if (chrono == HALF) {
				String[] rt = Func.toReadableTime((long) (Math.abs(HALF - PVP_T)) * 1000);
				Bukkit.broadcastMessage(Func.format("&aIl reste &3" + rt[0] + rt[1] + rt[2] + "&a avant le début des captures"));
			}
			if (chrono == FIFTH) {
				String[] rt = Func.toReadableTime((long) (Math.abs(FIFTH - PVP_T)) * 1000);
				Bukkit.broadcastMessage(Func.format("&eIl reste &3" + rt[0] + rt[1] + rt[2] + "&e avant le début des captures"));
			}
			if (chrono == FIFTEENTH) {
				String[] rt = Func.toReadableTime((long) (Math.abs(FIFTEENTH - PVP_T)) * 1000);
				Bukkit.broadcastMessage(Func.format("&6Il reste &3" + rt[0] + rt[1] + rt[2] + "&6 avant le début des captures"));
			}
			if (chrono == PVP_T) {
				Bukkit.broadcastMessage(Func.format("&4Captures et Combats Activés"));
				for (Player p : TeamMngr.getTeamedPlayers())
					p.playSound(Main.map.getSpawnLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, SoundCategory.MASTER, 400.0f, 1.0f);
				pvp = true;
			}
			chrono++;
		} else cancel();
	}
	
}
