package fr.loferga.lost_settlers.tasks;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;

public class NaturalRegen extends BukkitRunnable {
	
	private static final int DELAY = Main.getPlugin(Main.class).getConfig().getInt("regeneration_delay");
	
	private static boolean active = false;
	
	public static void start(Plugin plugin) {
		active = true;
		new NaturalRegen().runTaskTimer(plugin, 0L, 20L);
	}
	
	public static void stop() {
		active = false;
	}
	
	private static Map<Player, Integer> inCombat = new HashMap<>();
	
	public static void addPlayer(Player p) {
		inCombat.put(p, DELAY);
		p.addPotionEffect(new PotionEffect(PotionEffectType.UNLUCK, DELAY*20, 0, false, false, true));
	}
	
	public static boolean isInCombat(Player p) {
		return inCombat.containsKey(p);
	}
	
	@Override
	public void run() {
		if (active) {
			for (Player p : new HashSet<>(inCombat.keySet())) {
				int t = inCombat.get(p)-1;
				if (t == 0)
					inCombat.remove(p);
				else
					inCombat.replace(p, t);
			}
		} else cancel();
	}
	
}
