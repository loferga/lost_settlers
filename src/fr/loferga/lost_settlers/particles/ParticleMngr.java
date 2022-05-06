package fr.loferga.lost_settlers.particles;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ParticleMngr {
	
	private static class HubParticle extends BukkitRunnable {
		
		private static boolean active = true;

		@Override
		public void run() {
			if (active) {
				for (Player p : trail.keySet()) {
					Location ploc = p.getLocation();
					ParticleSetting ps = trail.get(p);
					ploc.getWorld().spawnParticle(ps.type, ploc.getX(), ploc.getY(), ploc.getZ(), 1, ps.offset, ps.offset, ps.offset, ps.speed);
				}
			} else cancel();
		}
		
	}
	
	private static class ParticleSetting {
		
		public ParticleSetting(Particle type, double offset, double speed) {
			this.type = type;
			this.offset = offset;
			this.speed = speed;
		}
		
		public Particle type;
		public double offset;
		public double speed;
		
	}
	
	public static void activeTrails(Plugin plugin) {
		new HubParticle().runTaskTimer(plugin, 0L, 1L);
	}
	
	public static void disableTrails() {
		HubParticle.active = false;
	}
	
	private static Map<Player, ParticleSetting> trail = new HashMap<>();
	
	public static void addTrail(Player p, Material mat) {
		if (mat == Material.BLACK_STAINED_GLASS_PANE) return;
		ParticleSetting particle = toSetting(mat);
		if (!trail.containsKey(p) || trail.get(p) != particle)
			trail.put(p, particle);
	}
	
	private static ParticleSetting toSetting(Material mat) {
		Particle type = toParticle(mat);
		double offset = 0.05;
		double speed = 0.001;
		if (type == Particle.ENCHANTMENT_TABLE) {
			offset = 0.0;
			speed = 0.2;
		}
		if (type == Particle.TOTEM)
			speed = 0.05;
		if (type == Particle.SOUL)
			speed = 0.01;
		return new ParticleSetting(type, offset, speed);
	}
	
	private static Particle toParticle(Material mat) {
		switch(mat) {
		case MUSIC_DISC_CAT: return Particle.VILLAGER_HAPPY;
		case MUSIC_DISC_FAR: return Particle.TOTEM;
		case MUSIC_DISC_MALL: return Particle.DRAGON_BREATH;
		case MUSIC_DISC_MELLOHI: return Particle.ENCHANTMENT_TABLE;
		case MUSIC_DISC_STAL: return Particle.SMOKE_NORMAL;
		case MUSIC_DISC_STRAD: return Particle.END_ROD;
		case MUSIC_DISC_11: return Particle.SOUL;
		case MUSIC_DISC_WAIT: return Particle.SOUL_FIRE_FLAME;
		default: return Particle.FLAME;
		}
	}
	
}