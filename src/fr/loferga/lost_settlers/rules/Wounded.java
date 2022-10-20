package fr.loferga.lost_settlers.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.game.GameMngr;

public class Wounded extends BukkitRunnable implements Listener {
	
//  # TRACKERS #
//   ENTRY
	@EventHandler
	public void onPlayerDamage(EntityDamageEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		
		if (GameMngr.gameIn(p) == null) return;
		if (e.getCause() == DamageCause.FIRE_TICK) return;
		
		addPlayer(p);
	}
	
//   WOUND EFFECT
	@EventHandler
	public void onPlayerRegainHealth(EntityRegainHealthEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		
		if (((Player) e.getEntity()).getNoDamageTicks() < DELAY*20) return;
		
		e.setCancelled(true);
	}
//  #
	
//  # SINGLETON #
	private static boolean running = false;
	
	private static Wounded wounded = null;
	
	public static Wounded getInstance() {
		if (wounded == null)
			wounded = new Wounded();
		return wounded;
	}
//  #
	
//  # PUBLIC INTERACTION #
	private static final int DELAY = Main.getPlugin(Main.class).getConfig().getInt("regeneration_delay");
	
	private static Map<Player, Integer> woundeds = new HashMap<>();
	
	private static void addPlayer(Player p) {
		woundeds.put(p, DELAY);
	}
	
//  #
	
//  # RUNNABLE #
	public void start(Plugin plugin) {
		if (!running) {
			this.runTaskTimer(plugin, 0L, 20L);
			running = true;
		}
	}
	
	@Override
	public void run() {
		for (Player p : new HashSet<>(woundeds.keySet())) {
			int t = woundeds.get(p)-1;
			if (t == 0)
				woundeds.remove(p);
			else
				woundeds.replace(p, t);
		}
	}
	
	public void stop() {
		running = false;
		cancel();
	}
//  #
	
}
