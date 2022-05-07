package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

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
	protected static double highest_ground;
	protected static List<LodeGenerator> generators = new ArrayList<>();
	
	public static void buildMapVars(ConfigurationSection cfg) {
		highest_ground = cfg.getDouble("lodes.highest_ground");
		buildGenerators(cfg.getConfigurationSection("lodes.ores"));
		CampMngr.buildCamps(cfg.getConfigurationSection("camps"));
	}
	
	public static void buildMap() {
		CampMngr.initFlags();
		generateLodes();
	}
	
	private static void buildGenerators(ConfigurationSection cfg) {
		for (String ore : cfg.getKeys(false)) {
			List<?> data = cfg.getList(ore);
			List<?> rv = (List<?>) data.get(3);
			generators.add(new LodeGenerator(
					Material.valueOf(ore.toUpperCase()),
					(double) data.get(0), (int) data.get(1), (double) data.get(2),
new double[] {(double) rv.get(1), (double) rv.get(1), (double) rv.get(2), (double) rv.get(3), (double) rv.get(4), (double) rv.get(5)},
					(int) data.get(4)
			));
		}
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
	
	private static void generateLodes() {
		for (LodeGenerator generator : generators) {
			createLode(generator, new double[2], new double[2]);
		}
	}
	
	public static void createLode(LodeGenerator g, double[] mins, double[] maxs) {
		/*
		double d = Main.map.getWorldBorder().getSize();
		double cx = Main.map.getWorldBorder().getCenter().getX();
		double cz = Main.map.getWorldBorder().getCenter().getZ();
		*/
		for (int n = 0; n<g.count; n++) {
			double y = Func.onBounds(0, 1, Func.gauss(g.gaussFactor)*g.gaussOffset) * (highest_ground-4) + 4;
			Vector[] ijk = randomVectors(2, 4.5, 1, 3.5, 1, 1.5);
			new Lode(g.ore, new Point(
					random(mins[0], maxs[0]),
					y,
					random(mins[1], maxs[1])
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
