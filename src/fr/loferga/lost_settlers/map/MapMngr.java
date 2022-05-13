package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.WorldBorder;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.camps.CampMngr;
import fr.loferga.lost_settlers.map.geometry.Point;
import fr.loferga.lost_settlers.map.geometry.Vector;

public class MapMngr {
	
	private static final List<Double> SPAWN = Main.getPlugin(Main.class).getConfig().getDoubleList("maps.spawn.location");
	private static final boolean JUMP = Main.getPlugin(Main.class).getConfig().getBoolean("maps.spawn.jump_active");
	private static final double MIN_HEIGHT = Main.getPlugin(Main.class).getConfig().getDouble("maps.spawn.jump.minimum_height");
	private static final Material CHECKPOINT = Material.valueOf(Main.getPlugin(Main.class).getConfig().getString("maps.spawn.jump.checkpoint_block").toUpperCase());
	private static final Material RESET = Material.valueOf(Main.getPlugin(Main.class).getConfig().getString("maps.spawn.jump.reset_block").toUpperCase());
	protected static boolean lodes;
	protected static double highest_ground;
	protected static List<LodeGenerator> generators = new ArrayList<>();
	private static final double RANGE = Main.getPlugin(Main.class).getConfig().getDouble("tp_range");
	
	public static void buildMapVars(ConfigurationSection cfg) {
		lodes = cfg.getBoolean("lodes_active");
		CampMngr.buildCamps(cfg.getConfigurationSection("camps"));
		if (lodes) {
			highest_ground = cfg.getDouble("lodes.highest_ground");
			buildGenerators(cfg.getConfigurationSection("lodes.ores"));
		}
	}
	
	public static void buildMap() {

		Bukkit.broadcastMessage("Initialisation des drapeaux ...");
		CampMngr.initFlags();
		setWorldBorder();
		if (lodes) {
			Bukkit.broadcastMessage("Generation des minerais ...");
			generateLodes();
		}
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
	
	/*
	 * ============================================================================
	 *                                   LODES
	 * ============================================================================
	 */
	
	private static void generateLodes() {
		double d = Main.map.getWorldBorder().getSize();
		double cx = Main.map.getWorldBorder().getCenter().getX();
		double cz = Main.map.getWorldBorder().getCenter().getZ();
		for (LodeGenerator generator : generators) {
			createLode(generator, new double[] {cx-d, 2, cz-d}, new double[] {cx+d, highest_ground,  cz+d});
			System.out.println(generator.ore.toString() + " generated");
		}
	}
	
	public static void createLode(LodeGenerator g, double[] mins, double[] maxs) {
		int count = (int) (g.count * (((maxs[0]-mins[0])*(maxs[1]-mins[1])*(maxs[2]-mins[2]))/1000000));
		double[] rv = g.sizeBounds;
		for (int n = 0; n<count; n++) {
			double x = random(mins[0], maxs[0]);
			double y = Func.onBounds(0, 1, Func.gauss(g.gaussFactor)*g.gaussOffset) * (maxs[1]-mins[1]) *g.yratio + mins[1];
			double z = random(mins[2], maxs[2]);
			Vector[] ijk = randomVectors(rv[0], rv[1], rv[2], rv[3], rv[4], rv[5]);
			new Lode(g.ore, new Point(
					x,
					y,
					z
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
	
	/*
	 * ============================================================================
	 *                                   MISC
	 * ============================================================================
	 */
	
	private static void setWorldBorder() {
		Location loc = MapMngr.getMapCenter();
		double maxDist = MapMngr.getMaxDist(loc);
		WorldBorder wb = Main.map.getWorldBorder();
		wb.setCenter(loc);
		wb.setSize((int) (maxDist + 150));
	}
	
	public static void campTeleport(Player p, Camp c) {
		p.teleport(c.getLoc().add(
				(Math.random()*2*RANGE)-RANGE,
				0,
				(Math.random()*2*RANGE)-RANGE
				));
	}
	
	public static void spawnTeleport(Player p) {
		p.teleport(new Location(Bukkit.getWorlds().get(0), SPAWN.get(0), SPAWN.get(1), SPAWN.get(2)));
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
	
	public static void checkJump(Player p, Location from, Location to) {
		if (JUMP) {
			if (to.getY() < MIN_HEIGHT) {
				p.setHealth(0);
			} else if (to.getBlock().getRelative(BlockFace.DOWN).getType() == CHECKPOINT) {
				Location abs = to.getBlock().getLocation().add(0.5, 0, 0.5);
				abs.setYaw(to.getYaw());
				p.setBedSpawnLocation(abs, true);
				Func.sendActionbar(p, Func.format("&eCheckpoint"));
			} else if (to.getBlock().getRelative(BlockFace.DOWN).getType() == RESET) {
				p.setBedSpawnLocation(null);
			}
		}
	}
	
}
