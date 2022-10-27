package fr.loferga.lost_settlers.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.game.GameMngr;

public class CombatTracker extends BukkitRunnable implements Listener {
	
//  # TRACKERS #
//  ENTRY
	private static final String SOURCE_RETRIEVE_FIELD_NAME = "Owner";
	
	@EventHandler
	public void onPlayerDamageByPlayer(EntityDamageByEntityEvent e) {
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if (GameMngr.gameIn(p) == null) return;
		
		if (e.getDamager() instanceof Projectile proj) {
			
			if (proj.getShooter() instanceof Player src)
				addPlayer(p, src);
			else if (proj.getShooter() instanceof BlockProjectileSource srcBlock) 
				
					addPlayer(p, getSource(srcBlock));
				
		} else if (e.getDamager() instanceof TNTPrimed tnt) {
			
			if (tnt.getSource() instanceof Player src)
				addPlayer(p, src);
			else if (tnt.getSource() instanceof BlockProjectileSource srcBlock)
				
				addPlayer(p, getSource(srcBlock));
			
		}
	}
	
	private static Player getSource(BlockProjectileSource bps) {
		UUID u = null;
		for (MetadataValue mv : bps.getBlock().getMetadata(SOURCE_RETRIEVE_FIELD_NAME))
			if (mv.getOwningPlugin() == Main.PLG) {
				u = (UUID) mv.value();
				break;
			}
		Player source = Bukkit.getPlayer(u);
		if (source == null) return null;
		return source;
	}
	
	// réusiner pour factoriser pour sortit blockSource retrieve hors de la fonction pour l'alléger
	
	@EventHandler
	public void onDispenserPlaced(BlockPlaceEvent e) {
		if (GameMngr.gameIn(e.getPlayer()) == null) return;
		if (e.getBlock().getType() != Material.DISPENSER) return;
		
		e.getBlock().setMetadata(SOURCE_RETRIEVE_FIELD_NAME, new FixedMetadataValue(Main.PLG, e.getPlayer().getUniqueId()));
	}
//  #
	
//  # SINGLETON #
	private boolean running = false;
	
	private static CombatTracker combatTracker = null;
	
	public static CombatTracker getInstance() {
		if (combatTracker == null)
			combatTracker = new CombatTracker();
		return combatTracker;
	}
//  #
	
//  # PUBLIC INTERACTION #
	private static final int DELAY = Main.PLG.getConfig().getInt("regeneration_delay");
	
	private static Map<Player, Object[]> inCombat = new HashMap<>();
	
	public static void addPlayer(final Player p, final Player k) {
		inCombat.put(p, new Object[] {DELAY, k});
	}
	
	public static boolean isInCombat(final Player p) {
		return inCombat.containsKey(p);
	}
//  #
	
//  # RUNNABLE #
	public void start() {
		if (running) return;
		
		runTaskTimer(Main.PLG, 0L, 20L);
		running = true;
	}
	
	@Override
	public void run() {
		for (Player p : new HashSet<>(inCombat.keySet())) {
			int t = (int) inCombat.get(p)[0] - 1;
			if (t == 0)
				inCombat.remove(p);
			else
				inCombat.replace(p, new Object[] {t, inCombat.get(p)[1]});
		}
	}
	
	public void stop() {
		running = false;
		cancel();
	}
//  #
	
}