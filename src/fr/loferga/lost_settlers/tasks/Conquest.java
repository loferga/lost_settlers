package fr.loferga.lost_settlers.tasks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.camps.CampMngr;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Conquest extends BukkitRunnable {
	
	private static boolean active = false;
	
	public static void start(Plugin plugin) {
		active = true;
		new Conquest().runTaskTimer(plugin, 0L, 1L);
	}
	
	public static void stop() {
		active = false;
	}
	
	private static Set<Camp> disputedCamps = new HashSet<>();
	
	public static void addConquest(Camp camp, Team team) {
		disputedCamps.add(camp);
		camp.addRival(team);
	}

	@Override
	public void run() {
		if (active) {
			for (Camp camp : disputedCamps) {
				for (Team team : new ArrayList<>(camp.getRivals())) {
					if (!CampMngr.teamProtect(team, camp)) {
						camp.removeRival(team);
						for (Player p : TeamMngr.getPlayers(team))
							p.playSound(camp.getLoc(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
					}
				}
				if (camp.getRivals().size() == 1) {
					CampMngr.capture(camp, camp.getRivals().get(0));
					disputedCamps.remove(camp);
				}
			}
		} else cancel();
	}

}
