package fr.loferga.lost_settlers.map.camps;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class CampMngr {
	
	public static Camp[] buildCamps(World world, ConfigurationSection cfg) {
		Set<String> keys = cfg.getConfigurationSection("positions").getKeys(false);
		Iterator<String> it = keys.iterator();
		Camp[] camps = new Camp[keys.size()];
		LSTeam[] teams = TeamMngr.get().clone();
		int i = 0;
		while (it.hasNext()) {
			String camp = it.next();
			List<?> data = cfg.getList("positions." + camp);
			LSTeam team = null;
			if (i<teams.length) {
				int rng = ThreadLocalRandom.current().nextInt(teams.length-i);
				team = teams[rng];
				int last = teams.length-1-i;
				if (last != rng) {
					LSTeam tmp = teams[rng];
					teams[rng] = teams[last];
					teams[last] = tmp;
				}
			}
			camps[i] = new Camp(
					camp,
					team,
					new Location(world, (double) data.get(0), (double) data.get(1), (double) data.get(2)),
					(boolean) data.get(3)
				);
 			i++;
		}
		return camps;
	}
	
}
