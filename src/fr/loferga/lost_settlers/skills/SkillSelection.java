package fr.loferga.lost_settlers.skills;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Main;

public class SkillSelection {
	
	private static FileConfiguration cfg = Main.getPlugin(Main.class).getConfig();
	
	private static Map<Skill, Set<Player>> selection = buildSelection();
	private static Map<Skill, Set<Player>> buildSelection() {
		Map<Skill, Set<Player>> res = new HashMap<>();
		for (Skill s : Skill.values()) {
			String path = "skills." + s.toString().toLowerCase();
			if (cfg.contains(path) && cfg.getBoolean(path))
				res.put(s, new HashSet<>());
		}
		return res;
	}
	
	public static Skill get(Player p) {
		for (Skill s : getSkills())
			if (selection.get(s).contains(p))
				return s;
		return null;
	}
	
	public static void select(Player p, String name) {
		for (Skill s : getSkills())
			if (selection.get(s).contains(p))
				selection.get(s).remove(p);
		try {
			Skill s = Skill.valueOf(name);
			if (selection.containsKey(s))
				selection.get(s).add(p);
		} catch (IllegalArgumentException e) {
			System.out.println(ChatColor.RED + "[LS] IllegalArgumentException at "
					+ "SkillSelection.select(Player p, String name), \"" + name +"\" is not a SkillName" );
		}
	}
	
	public static Set<Player> getSet(Skill s) {
		return selection.get(s);
	}
	
	private static final Skill[] SKILLS = buildSkillArray();
	private static Skill[] buildSkillArray() {
		Skill[] arr = new Skill[selection.size()];
		int i = 0;
		for (Skill s : Skill.values())
			if (selection.containsKey(s))
				arr[i++] = s;
		return arr;
	}
	
	public static Skill[] getSkills() {
		return SKILLS;
	}

}