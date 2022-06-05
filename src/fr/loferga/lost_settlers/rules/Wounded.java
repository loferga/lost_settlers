package fr.loferga.lost_settlers.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;

public class Wounded extends BukkitRunnable {
	
	private static final int DELAY = Main.getPlugin(Main.class).getConfig().getInt("regeneration_delay");
	
	private static Map<Player, Integer> inCombat = new HashMap<>();
	
	public static void addPlayer(Player p) {
		inCombat.put(p, DELAY);
	}
	
	public static boolean isInCombat(Player p) {
		return inCombat.containsKey(p);
	}
	
	public void stop() {
		cancel();
	}
	
	@Override
	public void run() {
		for (Player p : new HashSet<>(inCombat.keySet())) {
			int t = inCombat.get(p)-1;
			if (t == 0)
				inCombat.remove(p);
			else
				inCombat.replace(p, t);
		}
	}
	
}
