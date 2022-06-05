package fr.loferga.lost_settlers.dogs;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wolf;

public class Anger {
	
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
	
	public static void run() {
		for (Wolf wolf : targets.keySet())
			wolf.setTarget(targets.get(wolf));
	}

}
