package fr.loferga.lost_settlers.tasks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.map.camps.CampMngr;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class RespawnCooldown extends BukkitRunnable {
	
	private static boolean active = false;
	
	public static void start(Plugin plugin) {
		active = true;
		new RespawnCooldown().runTaskTimer(plugin, 0L, 20L);
	}
	
	public static void stop() {
		active = false;
	}
	
	private static Map<Player, Integer> dT = new HashMap<>();
	private static Map<Player, Integer> dN = new HashMap<>();
	
	public static void add(Player p) {
		if (!dN.containsKey(p))
			dN.put(p, 0);
		dT.put(p, (int) (30 * Math.pow(2, dN.get(p))));
		dN.replace(p, dN.get(p) + 1);
	}
	
	public static void remove(Player p) {
		dT.remove(p);
	}

	@Override
	public void run() {
		if (active) {
			for (Player p : dT.keySet()) {
				if (dT.get(p) <= 0) {
					if (CampMngr.getTeamCamps(TeamMngr.teamOf(p)).size() >= 1)
						TeamMngr.respawn(p);
					else
						p.sendMessage(Func.format("&cVous ne pouvez pas réapparaitre car vous n'avez pas de camp"));
					remove(p);
				} else {
					Func.sendActionbar(p, Func.format("&e" + dT.get(p)));
					dT.replace(p, dT.get(p) - 1);
				}
			}
		} else cancel();
	}
	
	
	
}
