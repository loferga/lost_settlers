package fr.loferga.lost_settlers.dogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Anger extends BukkitRunnable {
	
	private static boolean running = false;
	
	// Singleton methods
	private static Anger anger = null;
	
	public static Anger getInstance() {
		if (anger == null)
			anger = new Anger();
		return anger;
	}
	
	//start the runnable if it's not already running
	public void start(Plugin plugin) {
		if (!running) {
			runTaskTimer(plugin, 0L, 1L);
			running = true;
		}
	}
	
	private static Map<Wolf, LivingEntity> targets = new HashMap<>();
	
	public static void addAnger(Wolf wolf, LivingEntity target) {
		wolf.setSitting(false);
		targets.put(wolf, target);
	}
	
	public static void removeAnger(Wolf wolf) {
		targets.remove(wolf);
		wolf.setAngry(false);
	}
	
	public static boolean contain(Wolf wolf) {
		return targets.containsKey(wolf);
	}
	
	public static boolean containAnger(Wolf wolf, LivingEntity target) {
		return targets.containsKey(wolf) && targets.get(wolf) == target;
	}
	
	public static Set<Wolf> getDogAngryAt(LivingEntity target) {
		Set<Wolf> res = new HashSet<>();
		for (Wolf wolf : targets.keySet())
			if (targets.get(wolf) == target)
				res.add(wolf);
		return res;
	}
	
	@Override
	public void run() {
		for (Wolf wolf : targets.keySet())
			wolf.setTarget(targets.get(wolf));
	}

}
