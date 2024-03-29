package fr.loferga.lost_settlers.map;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.camps.Direction;
import fr.loferga.lost_settlers.util.Func;
import net.md_5.bungee.api.ChatColor;

public class MapSettings {
	
	private ConfigurationSection cfg;
	
	// optional world generic parameters
	public long seed = -1;
	public WorldType worldType = null;
	public String worldName = null;
	public int[] worldSpawn = null;
	
	// game launch required parameters
	public int teamNumber = -1;
	public int playableArea = -1;
	public int campSize = -1;
	public int vitalSize = -1;
	public Camp[] camps = null;
			
	// game optional parameters
	public double chamberHeight = -1.0;
	public int highestGround = -1;
	public LodeGenerator[] generators = null;
	
	public MapSettings(String wn) {
		cfg = Main.plg().getConfig().getConfigurationSection("maps." + wn);
		if (cfg.contains("seed", true)) seed = cfg.getLong("seed");
		if (cfg.contains("world_type", true)) worldType = Func.valueOf(WorldType.class, cfg.getString("world_type"));
		if (cfg.contains("world_name", true)) worldName = cfg.getString("world_name");
		if (cfg.contains("world_spawn", true)) worldSpawn = toIntArray(cfg.getIntegerList("world_spawn"));
		if (cfg.contains("team_number", true)) teamNumber = cfg.getInt("team_number");
		if (cfg.contains("playable_area", true)) playableArea = cfg.getInt("playable_area");
		if (cfg.contains("camps", true)) {
			if (cfg.contains("camps.camp_size", true)) campSize = cfg.getInt("camps.camp_size");
			if (cfg.contains("camps.vital_size", true)) vitalSize = cfg.getInt("camps.vital_size");
		}
		if (cfg.contains("chamber", true) && cfg.contains("chamber_height", true))
			chamberHeight = cfg.getDouble("chamber.chamber_height");
		if (cfg.contains("lodes", true)) {
			if (cfg.contains("lodes.highest_ground", true)) highestGround = cfg.getInt("lodes.highest_ground");
			if (cfg.contains("lodes.ores", true)) buildGenerators();
		}
	}
	
	private static int[] toIntArray(List<Integer> list) {
		int[] arr = new int[list.size()];
		for (int i = 0; i < list.size(); i++)
			arr[i] = list.get(i);
		return arr;
	}
	
	public boolean canHostGame() {
		boolean canHost = teamNumber != -1 && playableArea != -1 && campSize != -1 && vitalSize != -1 && camps != null;
		if (!canHost) printSetValues();
		return canHost;
	}
	
	private void printSetValues() {
		ConsoleCommandSender csl = Main.plg().getServer().getConsoleSender();
		if (teamNumber == -1)
			csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_RED + "team_number is not set in config.yml");
		else csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_GREEN + "team_number is set");
		if (playableArea == -1)
			csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_RED + "playable_area is not set in config.yml");
		else csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_GREEN + "playable_area is set");
		if (campSize == -1)
			csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_RED + "camp_size is not set in config.yml");
		else csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_GREEN + "camp_size is set");
		if (vitalSize == -1)
			csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_RED + "vital_size is not set in config.yml");
		else csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_GREEN + "vital_size is set");
		if (camps == null)
			csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_RED + "camps are not set in config.yml");
		else csl.sendMessage(Main.LOG_PREFIX + ChatColor.DARK_GREEN + "camps are set");
	}
	
	public boolean isSeedSet() {
		return seed != -1;
	}
	
	public boolean isWorldTypeSet() {
		return worldType != null;
	}
	
	public boolean isWorldNameSet() {
		return worldName != null;
	}
	
	public boolean isWorldSpawnSet() {
		return worldSpawn != null;
	}
	
	public boolean isChamberActive() {
		return chamberHeight != -1.0;
	}
	
	public boolean isLodesActive() {
		return generators != null && highestGround != -1;
	}
	
	private static final String CAMP_PREFIX = "camps.positions.";
	
	public void buildCamps(World world) {
		if (!cfg.contains("camps.positions", true)) return;
		Set<String> keys = cfg.getConfigurationSection(CAMP_PREFIX).getKeys(false);
		camps = new Camp[keys.size()];
		int i = 0;
		for (String camp : keys) {
			List<?> data = cfg.getList(CAMP_PREFIX + camp);
			Direction dir = null;
			try {
				dir = Direction.valueOf(((String) data.get(3)).toUpperCase());
			} catch (Exception e) {
				camps = null;
				System.out.println(data.get(3) + " is not a direction");
				return;
			}
			camps[i] = new Camp(
					camp,
					new Location(world, (double) data.get(0), (double) data.get(1), (double) data.get(2)),
					dir
				);
 			i++;
		}
	}
	
	private static final String LODES_PREFIX = "lodes.ores.";
	
	public void buildGenerators() {
		Set<String> keys = cfg.getConfigurationSection(LODES_PREFIX).getKeys(false);
		generators = new LodeGenerator[keys.size()];
		int i = 0;
		for (String ore : keys) {
			List<?> data = cfg.getList(LODES_PREFIX + ore);
			List<?> rv = (List<?>) data.get(3);
			generators[i] = new LodeGenerator(
					Material.valueOf(ore.toUpperCase()),
					(double) data.get(0), (int) data.get(1), (double) data.get(2),
new double[] {(double) rv.get(0), (double) rv.get(1), (double) rv.get(2), (double) rv.get(3), (double) rv.get(4), (double) rv.get(5)},
					(int) data.get(4)
			);
			i++;
		}
	}
	
}