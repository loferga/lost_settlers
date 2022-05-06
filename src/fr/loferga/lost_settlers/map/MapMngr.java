package fr.loferga.lost_settlers.map;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.camps.CampMngr;
import fr.loferga.lost_settlers.map.geometry.Point;
import fr.loferga.lost_settlers.map.geometry.Vector;

public class MapMngr {
	
	private static double range = Main.getPlugin(Main.class).getConfig().getDouble("tp_range");
	protected static double ground_level;
	// organized such as: [coal, iron, copper, gold, redstone, lapis, diamond, emerald, debris]
	private static Map<Material, Integer> lodes_count = new HashMap<>();
	
	private static final Material[] lodes = new Material[] {Material.COAL_ORE, Material.IRON_ORE, Material.COPPER_ORE, Material.GOLD_ORE,
			Material.REDSTONE_ORE, Material.LAPIS_ORE, Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.ANCIENT_DEBRIS};
	
	public static void buildMapVars(ConfigurationSection cfg) {
		ground_level = cfg.getDouble("lodes.ground_level");
		List<Integer> counts = cfg.getIntegerList("lodes.count");
		for (int i = 0; i<counts.size(); i++)
			lodes_count.put(lodes[i], counts.get(i));
		CampMngr.buildCamps(cfg.getConfigurationSection("camps"));
	}
	
	public static void buildMap() {
		CampMngr.initFlags();
		spawnLodes();
	}
	
	public static void teleportAround(Location loc, Player p) {
		p.teleport(loc.add(
				(Math.random()*range)-range/2,
				0,
				(Math.random()*range)-range/2
				));
	}

	public static Location getMapCenter() {
		double[] point = new double[] {0.0, 0.0};
		List<Camp> camps = CampMngr.get();
		for (Camp camp : camps) {
			double[] cpos = camp.getPos();
			point[0] += cpos[0];
			point[1] += cpos[2];
		}
		point[0] /= camps.size();
		point[1] /= camps.size();
		return new Location(Main.map, (int) point[0], 0, (int) point[1]);
	}
	
	public static double getMaxDist(Location loc) {
		double max = 0.0;
		for (Camp camp : CampMngr.get()) {
			double[] cpos = camp.getPos();
			double xdist = Math.abs(cpos[0] - loc.getX());
			double zdist = Math.abs(cpos[2] - loc.getZ());
			if (Math.max(xdist, zdist) > max)
				max = Math.max(xdist, zdist);
		}
		return max;
	}
	
	/*
	 * ============================================================================
	 *                                   LODES
	 * ============================================================================
	 */
	
	private static void spawnLodes() {
		for (Material ore : lodes_count.keySet()) {
			for (int n = 0; n<lodes_count.get(ore); n++) {
				spawnLode(ore, lodes_count.get(ore));
			}
		}
	}
	
	public static void spawnLode(Material ore, int count) {
		double d = Main.map.getWorldBorder().getSize();
		double cx = Main.map.getWorldBorder().getCenter().getX();
		double cz = Main.map.getWorldBorder().getCenter().getZ();
		for (int n = 0; n<count; n++) {
			double y = Func.onBounds(0, 1, Func.gauss(5)*1.3) * (ground_level-4) + 4;
			Vector[] ijk = randomVectors(2, 4.5, 1, 3.5, 1, 1.5);
			new Lode(Material.COAL_ORE, new Point(
					random(cx - d, cx + d),
					y,
					random(cz - d, cz + d)
			), ijk[0], ijk[1], ijk[2]).setMaterial();
		}
	}
	
	private static Vector[] randomVectors(double imin, double imax, double jmin, double jmax, double kmin, double kmax) {
		Vector[] ijk = new Vector[3];
		double ilen = random(imin, imax);
		double jlen = random(jmin, jmax);
		double klen = random(kmin, kmax);
		Vector i = new Vector(Math.random()*2-1, Math.random()*2-1, Math.random()*2-1).normalize().multiply(ilen);
		ijk[0] = i;
		Vector j = new Vector(ijk[0].y(), -ijk[0].x(), 0).normalize().multiply(jlen);
		ijk[1] = j;
		ijk[2] = new Vector(i.y()*j.z() - i.z()*j.y(), i.z()*j.x() - i.x()*j.z(), i.x()*j.y() - i.y()*j.x()).normalize().multiply(klen);
		return ijk;
	}
	
	private static double random(double min, double max) {
		return Math.random() * (max-min) + min;
	}
	
}
