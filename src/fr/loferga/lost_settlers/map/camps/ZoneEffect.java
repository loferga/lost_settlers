package fr.loferga.lost_settlers.map.camps;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.geometry.Vector;

public class ZoneEffect extends BukkitRunnable {
	
	private static final double STEP = 0.5;
	private static final Color NULL_COLOR = Color.GRAY;
	
	private Camp camp;
	
	private double vSize;
	
	public ZoneEffect(Camp camp) {
		this.camp = camp;
		this.vSize = MapMngr.getMapSettings(camp.getLocation().getWorld()).vitalSize;
		this.vSize -= this.vSize%STEP;
		runTaskTimer(Main.getPlugin(Main.class), 0L, 5L);
	}
	
	public void run() {
		Location loc = camp.getLocation().add(vSize, 0, vSize);
		Vector move = new Vector(-STEP, 0, 0);
		int sideLen = (int) (vSize*2);
		for (int side = 0; side<4; side++) {
			for (double i = 0.0; i<sideLen; i+=STEP) {
				placeParticle(loc);
				move.addTo(loc);
			}
			turnLeft(move);
		}
	}
	
	private void placeParticle(Location loc) {
		if (camp.getRivals().size() > 1)
			createParticle(loc, NULL_COLOR);
		else createParticle(loc, camp.getOwner().getColor());
	}
	
	private static void createParticle(Location loc, Color color) {
		loc.getWorld().spawnParticle(Particle.REDSTONE, loc, 4, new Particle.DustOptions(color, 0.8f));
	}
	
	private static void turnLeft(Vector vec) {
		if (vec.x()!=0) {
			vec.setZ(vec.x());
			vec.setX(0);
		} else {
			vec.setX(-vec.z());
			vec.setZ(0);
		}
	}
	
	public void stop() {
		cancel();
	}
	
}
