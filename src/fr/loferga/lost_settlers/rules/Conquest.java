package fr.loferga.lost_settlers.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Conquest extends BukkitRunnable {
	
	private Game game;
	private Set<Camp> disputedCamps = new HashSet<>();
	
	public Conquest(Game game) {
		this.game = game;
	}
	
	public void addConquest(Camp c, LSTeam t) {
		disputedCamps.add(c);
		c.addRival(t);
	}
	
	private void removeProtection(LSTeam t, Camp c) {
		if (!game.teamProtect(t, c)) return;
		
		c.removeRival(t);
		for (Player members : game.getTeamMembers(t))
			members.playSound(c.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
	}
	
	public void playerDie(Player p) {
		LSTeam t = TeamMngr.teamOf(p);
		for (Camp c : new HashSet<>(disputedCamps))
			if (c.getRivals().contains(t)) {
				removeProtection(t, c);
				break;
			}
	}
	
	public void playerGetOffVital(Player p, Camp c) {
		LSTeam t = TeamMngr.teamOf(p);
		if (c.getRivals().contains(t))
			removeProtection(TeamMngr.teamOf(p), c);
	}
	
	@Override
	public void run() {
		for (Camp camp : new HashSet<>(disputedCamps)) {
			for (LSTeam team : new ArrayList<>(camp.getRivals())) {
				if (!game.teamProtect(team, camp)) {
					camp.removeRival(team);
					for (Player p : game.getTeamMembers(team))
						p.playSound(camp.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
				}
			}
			if (camp.getRivals().size() == 1) {
				game.capture(camp, camp.getRivals().get(0));
				disputedCamps.remove(camp);
			}
		}
	}
	
}