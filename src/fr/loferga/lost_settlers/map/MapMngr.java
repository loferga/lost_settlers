package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.geometry.Point;
import fr.loferga.lost_settlers.map.geometry.Vector;

public class MapMngr {
	
	public static List<World> worlds = new ArrayList<>();
	private static List<MapSettings> mapsSettings = new ArrayList<>();
	
	private static final List<Double> SPAWN = Main.getPlugin(Main.class).getConfig().getDoubleList("maps.spawn.location");
	private static final double RANGE = Main.getPlugin(Main.class).getConfig().getDouble("tp_range");
	
	public static World newWorld(String wn) {
		World w = new WorldCreator("-LS-" + wn).createWorld();
		worlds.add(w);
		CloseWorld.addWorld(w);
		w.setAutoSave(false);
		return w;
	}
	
	public static void forget(World world, boolean save) {
		Bukkit.unloadWorld(world, save);
		Bukkit.getWorlds().remove(world);
		worlds.remove(world);
	}
	
	public static World getWorldFromName(String wn) {
		for (World w : worlds)
			if (w.getName().endsWith(wn))
				return w;
		return null;
	}
	
	public static void add(MapSettings ms) {
		mapsSettings.add(ms);
	}
	
	public static MapSettings getMapSettings(World world) {
		for (MapSettings ms : mapsSettings)
			if (ms.world == world)
				return ms;
		return null;
	}
	
	/*
	 * ============================================================================
	 *                                   LODES
	 * ============================================================================
	 */
	
	public static void generateLodes(World world, MapSettings ms) {
		double d = world.getWorldBorder().getSize();
		double cx = world.getWorldBorder().getCenter().getX();
		double cz = world.getWorldBorder().getCenter().getZ();
		for (LodeGenerator generator : ms.generators) {
			createLodes(world, generator, new double[] {cx-d, 2, cz-d}, new double[] {cx+d, ms.highestGround,  cz+d});
			System.out.println("[LodeGenerator] " + generator.ore.toString() + " generated");
		}
	}
	
	public static void createLodes(World world, LodeGenerator g, double[] mins, double[] maxs) {
		int count = (int) (g.count * (((maxs[0]-mins[0])*(maxs[1]-mins[1])*(maxs[2]-mins[2]))/1000000));
		double[] rv = g.sizeBounds;
		for (int n = 0; n<count; n++) {
			double x = Func.random(mins[0], maxs[0]);
			double y = Func.onBounds(0, 1, Func.gauss(g.gaussFactor)*g.gaussOffset) * (maxs[1]-mins[1]) *g.yratio + mins[1];
			double z = Func.random(mins[2], maxs[2]);
			Vector[] ijk = randomVectors(rv[0], rv[1], rv[2], rv[3], rv[4], rv[5]);
			new Lode(world, g.ore, new Point(
					x,
					y,
					z
			), ijk[0], ijk[1], ijk[2]).setMaterial();
		}
	}
	
	private static Vector[] randomVectors(double imin, double imax, double jmin, double jmax, double kmin, double kmax) {
		Vector[] ijk = new Vector[3];
		double ilen = Func.random(imin, imax);
		double jlen = Func.random(jmin, jmax);
		double klen = Func.random(kmin, kmax);
		Vector i = new Vector(Func.random(-1, 1), Func.random(-1, 1), Func.random(-1, 1)).normalize().multiply(ilen);
		ijk[0] = i;
		Vector j = new Vector(ijk[0].y(), -ijk[0].x(), 0).normalize().multiply(jlen);
		ijk[1] = j;
		ijk[2] = new Vector(i.y()*j.z() - i.z()*j.y(), i.z()*j.x() - i.x()*j.z(), i.x()*j.y() - i.y()*j.x()).normalize().multiply(klen);
		return ijk;
	}
	
	/*
	 * ============================================================================
	 *                                   MISC
	 * ============================================================================
	 */
	
	public static void setWorldBorder(World world, MapSettings mapSettings) {
		MapSettings ms = mapSettings;
		if (ms == null) ms = getMapSettings(world);
		if (ms != null) {
			Location loc = MapMngr.getMapCenter(world, ms);
			double maxDist = MapMngr.getMaxDist(world, loc, ms);
			WorldBorder wb = world.getWorldBorder();
			wb.setCenter(loc);
			wb.setSize((int) (maxDist + ms.playableArea));
		}
	}
	
	// A TRAVAILLER essentiel: garder en mémoir les blocks qui était là avant la pose du beacon
	
	public static Map<Location, Material> setBeams(MapSettings ms) {
		Map<Location, Material> mem = new HashMap<>();
		for (Camp c : ms.camps) {
			Location loc = c.getLocation().clone().add(0, -1, 0);
			setBlockType(loc.clone(), Material.valueOf(c.getOwner().getDyeColor().toString() + "_STAINED_GLASS"), mem);
			loc.add(0, -1, 0);
			setBlockType(loc.clone(), Material.BEACON, mem);
			loc.add(0, -1, 0);
			setBlockType(loc.clone(), Material.IRON_BLOCK, mem);
			double p = Math.PI;
			for (int i = 0; i<4; i++) {
				double[] t = new double[] {Math.cos(p), Math.sin(p)};
				loc.add(t[0], 0, t[1]);
				setBlockType(loc.clone(), Material.IRON_BLOCK, mem);
				loc.add(-2*t[0], 0, -2*t[1]);
				setBlockType(loc.clone(), Material.IRON_BLOCK, mem);
				loc.add(t[0], 0, t[1]);
				p += Math.PI/4;
			}
		}
		return mem;
	}
	
	private static void setBlockType(Location bloc, Material type, Map<Location, Material> map) {
		map.put(bloc, bloc.getBlock().getType());
		bloc.getBlock().setType(type);
	}
	
	public static void clearBeacons(Map<Location, Material> map) {
		for (Location loc : map.keySet())
			loc.getBlock().setType(map.get(loc));
	}

	public static Location getMapCenter(World world, MapSettings mapSettings) {
		Location mapCenter = null;
		MapSettings ms = mapSettings;
		if (ms == null) ms = getMapSettings(world);
		if (ms != null) {
			double[] point = new double[] {0.0, 0.0};
			Camp[] camps = ms.camps;
			for (Camp camp : camps) {
				double[] cpos = camp.getPosition();
				point[0] += cpos[0];
				point[1] += cpos[2];
			}
			point[0] /= camps.length;
			point[1] /= camps.length;
			mapCenter = new Location(world, point[0], ms.world.getHighestBlockYAt((int) point[0], (int) point[1]) + 5, point[1]);
		}
		return mapCenter;
	}
	
	public static double getMaxDist(World world, Location loc, MapSettings mapSettings) {
		double max = -1.0;
		MapSettings ms = mapSettings;
		if (ms == null) ms = getMapSettings(world);
		if (ms != null) {
			for (Camp camp : ms.camps) {
				double[] cpos = camp.getPosition();
				double xdist = Math.abs(cpos[0] - loc.getX());
				double zdist = Math.abs(cpos[2] - loc.getZ());
				if (Math.max(xdist, zdist) > max)
					max = Math.max(xdist, zdist);
			}
		}
		return max;
	}
	
	public static void campTeleport(Player p, Camp c) {
		p.teleport(c.getLocation().add(
				Func.random(-RANGE, RANGE),
				0,
				Func.random(-RANGE, RANGE)
				));
	}
	
	public static void spawnTeleport(Player p) {
		p.teleport(new Location(Bukkit.getWorlds().get(0), SPAWN.get(0), SPAWN.get(1), SPAWN.get(2)));
	}
	
}
