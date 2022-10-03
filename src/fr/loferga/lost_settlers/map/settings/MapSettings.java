package fr.loferga.lost_settlers.map.settings;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.LodeGenerator;
import fr.loferga.lost_settlers.map.camps.Camp;
import net.md_5.bungee.api.ChatColor;

public class MapSettings {
			
	// game launch required parameters
	public Integer playableArea = null;
	public Integer campSize = null;
	public Integer vitalSize = null;
	public Camp[] camps = null;
			
	// game optional parameters
	public Double chamberHeight = null;
	public Integer highestGround = null;
	public LodeGenerator[] generators = null;
	
	protected MapSettings(Integer playableArea, Integer campSize, Integer vitalSize, Camp[] camps, Double chamberHeight,
			Integer highestGround, LodeGenerator[] generators) {
		this.playableArea = playableArea;
		this.campSize = campSize;
		this.vitalSize = vitalSize;
		this.camps = camps;
		this.chamberHeight = chamberHeight;
		this.highestGround = highestGround;
		this.generators = generators;
	}
	
	public boolean canHostGame() {
		boolean canHost = playableArea != null && campSize != null && vitalSize != null && camps != null;
		if (!canHost) printSetValues();
		return canHost;
	}
	
	private void printSetValues() {
		ConsoleCommandSender csl = Main.getPlugin(Main.class).getServer().getConsoleSender();
		if (playableArea == null)
			csl.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "playable_area is not set in config.yml");
		else csl.sendMessage("[LostSettlers] " + ChatColor.DARK_GREEN + "playable_area is set");
		if (campSize == null)
			csl.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "camp_size is not set in config.yml");
		else csl.sendMessage("[LostSettlers] " + ChatColor.DARK_GREEN + "camp_size is set");
		if (vitalSize == null)
			csl.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "vital_size is not set in config.yml");
		else csl.sendMessage("[LostSettlers] " + ChatColor.DARK_GREEN + "vital_size is set");
		if (camps == null)
			csl.sendMessage("[LostSettlers] " + ChatColor.DARK_RED + "camps are not set in config.yml");
		else csl.sendMessage("[LostSettlers] " + ChatColor.DARK_GREEN + "camps are set");
	}
	
	public boolean isChamberActive() {
		return chamberHeight != null;
	}
	
	public boolean isLodesActive() {
		return generators != null && highestGround != null;
	}
	
	public static MapSettings retrieveData(World world) {
		MapSettingsBuilder b = new MapSettingsBuilder();
		ConfigurationSection cfg = Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps." + world.getName().substring(3));
		if (cfg.contains("playable_area")) b.withPlayableArea(cfg.getInt("playable_area"));
		if (cfg.contains("camps")) {
			if (cfg.contains("camps.camp_size")) b.withCampSize(cfg.getInt("camps.camp_size"));
			if (cfg.contains("camps.vital_size")) b.withVitalSize(cfg.getInt("camps.vital_size"));
			if (cfg.contains("camps.positions")) b.withCamps(buildCamps(world, cfg.getConfigurationSection("camps.positions")));
		}
		if (cfg.contains("chamber")) {
			if (cfg.contains("chamber_height")) b.withChamberHeight(cfg.getDouble("chamber.chamber_height"));
		}
		if (cfg.contains("lodes")) {
			if (cfg.contains("lodes.highest_ground")) b.withHighestGround(cfg.getInt("lodes.highest_ground"));
			if (cfg.contains("lodes.ores")) b.withGenerators(buildGenerators(cfg.getConfigurationSection("lodes.ores")));
		}
		return b.build();
	}
	
	// TODO
	public static Camp[] buildCamps(World world, ConfigurationSection cfg) {
		Set<String> keys = cfg.getKeys(false);
		Camp[] camps = new Camp[keys.size()];
		int i = 0;
		for (String camp : keys) {
			List<?> data = cfg.getList(camp);
			camps[i] = new Camp(
					camp,
					null,
					new Location(world, (double) data.get(0), (double) data.get(1), (double) data.get(2)),
					(boolean) data.get(3)
				);
 			i++;
		}
		return camps;
	}
	
	// TODO
	public static LodeGenerator[] buildGenerators(ConfigurationSection cfg) {
		Set<String> keys = cfg.getKeys(false);
		LodeGenerator[] generators = new LodeGenerator[keys.size()];
		int i = 0;
		for (String ore : keys) {
			List<?> data = cfg.getList(ore);
			List<?> rv = (List<?>) data.get(3);
			generators[i] = new LodeGenerator(
					Material.valueOf(ore.toUpperCase()),
					(double) data.get(0), (int) data.get(1), (double) data.get(2),
new double[] {(double) rv.get(0), (double) rv.get(1), (double) rv.get(2), (double) rv.get(3), (double) rv.get(4), (double) rv.get(5)},
					(int) data.get(4)
			);
			i++;
		}
		return generators;
	}
	
}