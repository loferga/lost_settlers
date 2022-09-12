package fr.loferga.lost_settlers.map;

import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import fr.loferga.lost_settlers.map.camps.Camp;

public class MapSettings {
	
	public MapSettings(World world, ConfigurationSection cfg) {
		this.world = world;
		this.playableArea = cfg.getInt("playable_area");
		this.chamber = cfg.getBoolean("chamber_active");
		this.lodes = cfg.getBoolean("lodes_active");
		
		this.camps = new Camp[cfg.getConfigurationSection("camps.positions").getKeys(false).size()];
		buildCamps(cfg.getConfigurationSection("camps"));
		this.cSize = cfg.getDouble("camps.settings.camp_size");
		this.vSize = cfg.getDouble("camps.settings.vital_size");
		
		this.highestGround = cfg.getDouble("highest_ground");
		if (chamber)
			this.chamberHeight = cfg.getDouble("chamber.max_height");
		if (lodes) {
			this.generators = new LodeGenerator[cfg.getConfigurationSection("lodes.ores").getKeys(false).size()];
			buildGenerators(cfg.getConfigurationSection("lodes.ores"));
		}
	}
	
	private void buildCamps(ConfigurationSection cfg) {
		Set<String> keys = cfg.getConfigurationSection("positions").getKeys(false);
		int i = 0;
		for (String camp : keys) {
			List<?> data = cfg.getList("positions." + camp);
			camps[i] = new Camp(
					camp,
					null,
					new Location(world, (double) data.get(0), (double) data.get(1), (double) data.get(2)),
					(boolean) data.get(3)
				);
 			i++;
		}
	}
	
	private void buildGenerators(ConfigurationSection cfg) {
		int i = 0;
		for (String ore : cfg.getKeys(false)) {
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
	}
	
	public World world;
	
	public int playableArea;
	public Camp[] camps;
	public double cSize;
	public double vSize;
	public double highestGround;
	public boolean chamber;
	public double chamberHeight;
	public boolean lodes;
	public LodeGenerator[] generators;
	
}