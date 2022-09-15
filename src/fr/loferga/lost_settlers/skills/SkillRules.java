package fr.loferga.lost_settlers.skills;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.game.GameMngr;

public class SkillRules extends BukkitRunnable {
	
	private static boolean running = false;
	
	// Singleton methods
	private static SkillRules skills = null;
	
	public static SkillRules getInstance() {
		if (skills == null)
			skills = new SkillRules();
		return skills;
	}
	
	//start the runnable if it's not already running
	public void start(Plugin plugin) {
		if (!running) {
			runTaskTimer(plugin, 0L, 1L);
			running = true;
		}
	}
	
	public void stop() {
		running = false;
		cancel();
	}
	
	private static final boolean clairvoyant = Func.primeContain(SkillSelection.getSkills(), Skill.CLAIRVOYANT);
	
	@Override
	public void run() {
		// Lucide
		if (clairvoyant)
			for (Player c : SkillSelection.getSet(Skill.CLAIRVOYANT))
				if (GameMngr.gameIn(c)!=null)
					for (Player other : GameMngr.gameIn(c).getAliveEnnemies(c))
						if (!other.isInvisible())
							if (other.getLocation().distance(c.getLocation()) < 10)
								Func.glowFor(other, new HashSet<>(Set.of(c)), 2);
	}
	
}