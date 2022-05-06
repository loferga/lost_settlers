package fr.loferga.lost_settlers.tasks;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.teams.TeamMngr;

public class CampZoneEffects extends BukkitRunnable {
	
	private static boolean active = false;
	
	public static void start(Plugin plugin) {
		active = true;
		new CampZoneEffects().runTaskTimer(null, 0L, 10L);
	}
	
	public static void stop() {
		active = false;
	}

	@Override
	public void run() {
		if (active) {
			for (Player p : TeamMngr.getTeamedPlayers()) {
				p.updateInventory();
			}
		} else cancel();
	}

}
