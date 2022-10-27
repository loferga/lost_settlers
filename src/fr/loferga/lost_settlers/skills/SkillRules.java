package fr.loferga.lost_settlers.skills;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.util.Func;
import fr.loferga.lost_settlers.util.GlowMngr;

public class SkillRules extends BukkitRunnable {
	
	private boolean running = false;
	
	// Singleton methods
	private static SkillRules skills = null;
	
	public static SkillRules getInstance() {
		if (skills == null)
			skills = new SkillRules();
		return skills;
	}
	
	//start the runnable if it's not already running
	public void start() {
		if (running) return;
		
		runTaskTimer(Main.plg(), 0L, 1L);
		running = true;
	}
	
	public void stop() {
		running = false;
		cancel();
	}
	
	private static final boolean CLAIRVOYANCE = Func.primeContain(SkillSelection.getSkills(), Skill.CLAIRVOYANCE);
	
	@Override
	public void run() {
		// Lucide
		if (CLAIRVOYANCE)
			for (Player c : SkillSelection.getSet(Skill.CLAIRVOYANCE))
				if (GameMngr.gameIn(c)!=null)
					for (Player other : GameMngr.gameIn(c).getAliveEnnemies(c))
						if (!other.isInvisible()
						&& other.getLocation().distance(c.getLocation()) < 10)
							GlowMngr.glowFor(other, new HashSet<>(Set.of(c)), 2);
	}
	
}