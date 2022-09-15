package fr.loferga.lost_settlers.dogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ComeBack extends BukkitRunnable {
	
	private static boolean running = false;
	
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
		for (LivingEntity ent : targets.keySet()) {
			if (targets.get(ent).contains(wolf)) {
				if (targets.get(ent).size() > 1) {
					targets.get(ent).remove(wolf);
				} else {
					targets.remove(ent);
					teleport.remove(ent);
					ent.remove();
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
		for (LivingEntity ent : targets.keySet()) {
			Set<Wolf> wset = new HashSet<>();
			wset.addAll(targets.get(ent));
			for (Wolf wolf : wset) {
				if (wolf.getLocation().distance(ent.getLocation()) > 4)
					wolf.setTarget(ent);
				else {
					removeDog(wolf);
					wolf.setAngry(false);
					wolf.setSilent(false);
				}
			}
		}
		for (LivingEntity ent : teleport.keySet())
			ent.teleport(teleport.get(ent));
	}

}
