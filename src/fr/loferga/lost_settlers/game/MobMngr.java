package fr.loferga.lost_settlers.game;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Entity;

public class MobMngr {
	
	private static Map<Integer, Map<Entity, Integer>> entities = new HashMap<>();
	
	public static boolean addEntity(Entity ent) {
		int layer = ((int) (ent.getLocation().getY()/10)) * 10;
		Map<Entity, Integer> maxent = entities.get(layer);
		if (maxent.containsKey(ent)) {
			if (maxent.get(ent) > 10)
				return false;
			maxent.replace(ent, maxent.get(ent) + 1);
		} else {
			maxent.put(ent, 0);
		}
		return true;
	}
	
}