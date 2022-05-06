package fr.loferga.lost_settlers.tasks;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class DogAnger extends BukkitRunnable {
	
	private static boolean active = false;
	
	public static void start(Plugin plugin) {
		active = true;
		new DogAnger().runTaskTimer(plugin, 0L, 1L);
	}
	
	public static void stop() {
		active = false;
	}
	
	private static Map<Wolf, LivingEntity> targets = new HashMap<>();
	
	public static void addAnger(Wolf wolf, LivingEntity target) {
		wolf.setSitting(false);
		targets.put(wolf, target);
	}
	
	public static void removeAnger(Wolf wolf) {
		targets.remove(wolf);
	}
	
	public static boolean contain(Wolf wolf) {
		return targets.containsKey(wolf);
	}
	
	public static boolean containAnger(Wolf wolf, LivingEntity target) {
		return targets.containsKey(wolf) && targets.get(wolf) == target;
	}
	
	public static Wolf getDogAngry(LivingEntity target) {
		for (Wolf wolf : targets.keySet()) {
			if (targets.get(wolf) == target) {
				return wolf;
			}
		}
		return null;
	}
	
	@Override
	public void run() {
		if (active) {
			for (Wolf wolf : targets.keySet())
				wolf.setTarget(targets.get(wolf));
		} else cancel();
	}

}
