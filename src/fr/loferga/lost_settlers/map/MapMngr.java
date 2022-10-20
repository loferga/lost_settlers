package fr.loferga.lost_settlers.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.gui.GUIMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.geometry.Point;
import fr.loferga.lost_settlers.map.geometry.Vector;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.BiMap;
import fr.loferga.lost_settlers.util.Func;

public class MapMngr {
	
	private MapMngr() {/*fonction holder class, it should never be instantiated*/}
	
	public static final boolean AUTO_LOAD = Main.plg.getConfig().contains("preload_worlds", true)
			&& Main.plg.getConfig().getBoolean("preload_worlds");
	
	private static BiMap<World, MapSettings> mapsSettings = new BiMap<>();
	public static final World HUB = newWorld("lobby");
	
	private static final int[] SPAWN = mapsSettings.get(HUB).worldSpawn;
	private static final double RANGE = Main.plg.getConfig().getDouble("tp_range");
	
	private static final String WORLD_NAME_PREFIX = "ls_";
	
	public static World newWorld(String wn) {
		MapSettings ms = getFromName(wn);
		if (ms == null) ms = new MapSettings(wn);
		World w = createWorld(wn, ms);
		w.setGameRule(GameRule.DISABLE_RAIDS, true);
		mapsSettings.put(w, ms);
		return w;
	}
	
	private static MapSettings getFromName(String wn) {
		World w = getWorldFromName(wn);
		if (w != null)
			return mapsSettings.get(w);
		return null;
	}
	
	private static World createWorld(String wn, MapSettings ms) {
		String name = wn;
		if (ms.isWorldNameSet()) name = ms.worldName;
		else if (!name.startsWith(WORLD_NAME_PREFIX)) name = WORLD_NAME_PREFIX + name;
		WorldCreator wc = new WorldCreator(name);
		
		if (ms.isSeedSet()) wc.seed(ms.seed);
		if (ms.isWorldTypeSet()) wc.type(ms.worldType);
		World w = wc.createWorld();
		if (ms.isWorldSpawnSet()) w.setSpawnLocation(ms.worldSpawn[0], ms.worldSpawn[1], ms.worldSpawn[2]);
		ms.buildCamps(w);
		w.setAutoSave(false);
		
		return w;
	}
	
	public static Set<World> getWorlds() {
		Set<World> w = mapsSettings.keySet();
		w.remove(HUB);
		return w;
	}
	
	public static void forget(World world, boolean save) {
		if (AUTO_LOAD && save)
			world.save();
		else {
			Bukkit.unloadWorld(world, save);
			if (!AUTO_LOAD)
				mapsSettings.remove(world);
			else
				newWorld(world.getName());
		}
	}
	
	public static World getWorldFromName(String wn) {
		for (World w : mapsSettings.keySet())
			if (w.getName().endsWith(wn))
				return w;
		return null;
	}
	
	public static boolean isMap(World w) {
		return mapsSettings.containsKey(w);
	}
	
	public static void add(World w, MapSettings ms) {
		mapsSettings.put(w, ms);
	}
	
	public static MapSettings getMapSettings(World world) {
		return mapsSettings.get(world);
	}
	
	public static World getWorld(MapSettings ms) {
		return mapsSettings.getKey(ms);
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
			createLodes(world, generator, new double[] {cx-d, -63, cz-d}, new double[] {cx+d, ms.highestGround,  cz+d});
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
		Vector j = new Vector(ijk[0].y, -ijk[0].x, 0).normalize().multiply(jlen);
		ijk[1] = j;
		ijk[2] = new Vector(i.y*j.z - i.z*j.y, i.z*j.x - i.x*j.z, i.x*j.y - i.y*j.x).normalize().multiply(klen);
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
	
	// BEACONS
	
	public static Map<Location, Material> setMap(MapSettings ms) {
		Map<Location, Material> mem = new HashMap<>();
		for (Camp c : ms.camps) {
			c.startZoneEffect();
			// BEAMS
			Location beamLoc = c.getLocation().clone().add(0, -1, 0);
			setBlockType(beamLoc.clone(), Material.valueOf(c.getOwner().getDyeColor().toString() + "_STAINED_GLASS"), mem);
			beamLoc.add(0, -1, 0);
			setBlockType(beamLoc.clone(), Material.BEACON, mem);
			beamLoc.add(0, -1, 0);
			setBlockType(beamLoc.clone(), Material.IRON_BLOCK, mem);
			double p = Math.PI;
			// PYRAMID
			for (int i = 0; i<4; i++) {
				double[] t = new double[] {Math.cos(p), Math.sin(p)};
				beamLoc.add(t[0], 0, t[1]);
				setBlockType(beamLoc.clone(), Material.IRON_BLOCK, mem);
				beamLoc.add(-2*t[0], 0, -2*t[1]);
				setBlockType(beamLoc.clone(), Material.IRON_BLOCK, mem);
				beamLoc.add(t[0], 0, t[1]);
				p += Math.PI/4;
			}
			// BARRIER
			Location barrierLoc = c.getLocation().clone().add(RANGE, 1, RANGE+1);
			Vector move = new Vector(-1, 0, 0);
			int sideLen = ((int) (RANGE+1)*2);
			for (int side = 0; side<4; side++) {
				for (int i = 0; i<sideLen; i++) {
					setBlockType(barrierLoc.clone(), Material.BARRIER, mem);
					move.addTo(barrierLoc);
				}
				turnLeft(move);
				move.addTo(barrierLoc);
			}
			
		}
		return mem;
	}
	
	private static void setBlockType(Location bloc, Material type, Map<Location, Material> map) {
		map.put(bloc, bloc.getBlock().getType());
		bloc.getBlock().setType(type);
	}
	
	private static void turnLeft(Vector vec) {
		if (vec.x != 0) {
			vec.z = vec.x;
			vec.x = 0;
		} else {
			vec.x = -vec.z;
			vec.z = 0;
		}
	}
	
	public static void clearMap(Map<Location, Material> map) {
		for (Entry<Location, Material> entry : map.entrySet())
			entry.getKey().getBlock().setType(entry.getValue());
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
			mapCenter = new Location(world, point[0], (double) world.getHighestBlockYAt((int) point[0], (int) point[1]) + 5, point[1]);
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
		p.getInventory().clear();
		GUIMngr.giveSelector(p);
		p.setGameMode(GameMode.ADVENTURE);
		if (TeamMngr.teamOf(p) == null) TeamMngr.join(p, TeamMngr.NULL);
		p.teleport(new Location(HUB, SPAWN[0] + 0.5, SPAWN[1], SPAWN[2] + 0.5));
	}
	
}
