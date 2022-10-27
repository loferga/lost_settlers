package fr.loferga.lost_settlers.dogs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;

public class Anger extends BukkitRunnable {
	
	private boolean running = false;
	
	// Singleton methods
	private static Anger anger = null;
	
	public static Anger getInstance() {
		if (anger == null)
			anger = new Anger();
		return anger;
	}
	
	//start the runnable if it's not already running
	public void start() {
		if (running) return;
		
		runTaskTimer(Main.plg(), 0L, 1L);
		running = true;
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
		for (Entry<Wolf, LivingEntity> e : targets.entrySet())
			if (e.getValue() == target)
				res.add(e.getKey());
		return res;
	}
	
	@Override
	public void run() {
		for (Entry<Wolf, LivingEntity> e : targets.entrySet())
			e.getKey().setTarget(e.getValue());
	}

}
