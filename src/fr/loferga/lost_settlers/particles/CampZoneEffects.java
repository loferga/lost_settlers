package fr.loferga.lost_settlers.particles;

import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class CampZoneEffects {
	
	public void run() {
		// modifier
		for (Player p : TeamMngr.teamedPlayers(Main.hub)) {
			p.updateInventory();
		}
	}

}
