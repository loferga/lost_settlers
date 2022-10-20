package fr.loferga.lost_settlers.dogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ComeBack extends BukkitRunnable {
	
	private boolean running = false;
	
	// Singleton methods
	private static ComeBack comeBack = null;
	
	public static ComeBack getInstance() {
		if (comeBack == null)
			comeBack = new ComeBack();
		return comeBack;
	}
	
	//start the runnable if it's not already running
	public void start(Plugin plugin) {
		if (!running) {
			runTaskTimer(plugin, 0L, 1L);
			running = true;
		}
	}
	
	private static Map<LivingEntity, Set<Wolf>> targets = new HashMap<>();
	private static Map<LivingEntity, Player> teleport = new HashMap<>();
	
	public static void addDog(Wolf wolf, LivingEntity target, Player p) {
		if (targets.containsKey(target)) {
			targets.get(target).add(wolf);
		} else {
			targets.put(target, new HashSet<>(Set.of(wolf)));
			teleport.put(target, p);
		}
		wolf.setSilent(true);
	}
	
	public static void removeDog(Wolf wolf) {
		for (Entry<LivingEntity, Set<Wolf>> e : targets.entrySet()) {
			if (e.getValue().contains(wolf)) {
				if (e.getValue().size() > 1) {
					e.getValue().remove(wolf);
				} else {
					targets.remove(e.getKey());
					teleport.remove(e.getKey());
					e.getKey().remove();
				}
			}
		}
	}
	
	public static boolean contain(Wolf wolf) {
		for (Set<Wolf> set : targets.values())
			if (set.contains(wolf))
				return true;
		return false;
	}
	
	public static boolean containPlayer(Player p) {
		for (Player tp : teleport.values()) {
			if (tp == p)
				return true;
		}
		return false;
	}
	
	@Override
	public void run() {
		for (Entry<LivingEntity, Set<Wolf>> e : targets.entrySet()) {
			Set<Wolf> wset = new HashSet<>();
			wset.addAll(e.getValue());
			for (Wolf wolf : wset) {
				if (wolf.getLocation().distance(e.getKey().getLocation()) > 4)
					wolf.setTarget(e.getKey());
				else {
					removeDog(wolf);
					wolf.setAngry(false);
					wolf.setSilent(false);
				}
			}
		}
		for (Entry<LivingEntity, Player> e : teleport.entrySet())
			e.getKey().teleport(e.getValue());
	}

}
