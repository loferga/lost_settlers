package fr.loferga.lost_settlers.rules;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.dogs.Anger;
import fr.loferga.lost_settlers.dogs.ComeBack;

public class RuleManager extends BukkitRunnable {
	
	public RuleManager() {
		Plugin plugin = Main.getPlugin(Main.class);
		Wounded.getInstance().start(plugin);
		SkillRules.getInstance().start(plugin);
		this.runTaskTimer(plugin, 0L, 1L);
	}
	
	@Override
	public void run() {
		Anger.run();
		ComeBack.run();
	}

	public void stop() {
		cancel();
	}
	
}