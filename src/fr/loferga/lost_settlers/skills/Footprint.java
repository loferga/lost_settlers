package fr.loferga.lost_settlers.skills;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Footprint extends BukkitRunnable {
	
	private Location location;
	private LSTeam team;
	private int maxD;
	private int duration;
	
	public Footprint(Player p) {
		location = p.getLocation();
		team = TeamMngr.teamOf(p);
		maxD = SkillSelection.get(p) == Skill.PISTAGE ? 300 : 900;
		duration = maxD;
	}

	@Override
	public void run() {
		for (Player p : SkillSelection.getSet(Skill.PISTAGE))
			if (p.getWorld() == location.getWorld())
				createParticle(p);
	}
	
	private void createParticle(Player p) {
		double ratio = (double) duration / maxD;
		int[] c = new int[] {255, 0, 0};
		if (TeamMngr.teamOf(p) == team)
			c = new int[] {0, 255, 180};
		for (int i = 0; i<3; i++) c[i] = (int) (ratio * c[i]);
		p.spawnParticle(Particle.REDSTONE, location, 4, Color.fromRGB(c[0], c[1], c[2]));
	}

}