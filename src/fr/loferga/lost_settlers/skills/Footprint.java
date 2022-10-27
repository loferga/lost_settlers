package fr.loferga.lost_settlers.skills;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;
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
		maxD = SkillSelection.get(p) == Skill.PISTAGE ? 15 : 30;
		duration = maxD;
		runTaskTimer(Main.PLG, 0L, 20L);
	}

	@Override
	public void run() {
		if (duration-- <= 0) cancel();
		for (Player p : SkillSelection.getSet(Skill.PISTAGE)) {
			if (p.getWorld() == location.getWorld())
				createParticle(p);
		}
	}
	
	private void createParticle(Player p) {
		double ratio = c1aff((double) duration / maxD, 0.9);
		int[] c = new int[] {255, 0, 0};
		if (TeamMngr.teamOf(p) == team)
			c = new int[] {0, 255, 180};
		for (int i = 0; i<3; i++) c[i] = (int) (ratio * c[i]);
		p.spawnParticle(Particle.REDSTONE, location, 4, new Particle.DustOptions(Color.fromRGB(c[0], c[1], c[2]), (float) c1aff(ratio, 0.5)));
	}
	
	private static double c1aff(double x, double a) {
		return a * x + (1-a);
	}

}