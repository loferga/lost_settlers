package fr.loferga.lost_settlers.rules;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.skills.Skill;
import fr.loferga.lost_settlers.skills.SkillSelection;

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
	
	private static final boolean lucide = Func.primeContain(SkillSelection.getSkills(), Skill.CLAIRVOYANT);
	
	@Override
	public void run() {
		// Lucide
		if (lucide)
			for (Player lucid : SkillSelection.getSet(Skill.CLAIRVOYANT))
				if (GameMngr.gameIn(lucid)!=null)
					for (Player other : GameMngr.gameIn(lucid).getAliveEnnemies(lucid))
						if (!other.isInvisible())
							if (other.getLocation().distance(lucid.getLocation()) < 6)
								Func.glowFor(other, new HashSet<>(Set.of(lucid)), 2);
	}
	
}