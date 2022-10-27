package fr.loferga.lost_settlers.skills;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Main;

public class SkillSelection {
	
	private SkillSelection() {/*fonction holder class, it should never be instantiated*/}
	
	private static FileConfiguration cfg = Main.PLG.getConfig();
	
	private static Map<Skill, Set<Player>> selection = buildSelection();
	private static Map<Skill, Set<Player>> buildSelection() {
		Map<Skill, Set<Player>> res = new EnumMap<>(Skill.class);
		for (Skill s : Skill.values()) {
			String path = "skills." + s.toString().toLowerCase();
			if (cfg.contains(path, true) && cfg.getBoolean(path))
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
	
	public static boolean select(Player p, String name) {
		try {
			select(p, Skill.valueOf(name));
			return true;
		} catch (IllegalArgumentException e) {
			System.out.println(ChatColor.RED + "[LS] IllegalArgumentException occured at "
					+ "skill selection, \"" + name +"\" is not a SkillName" );
		}
		return false;
	}
	
	public static void select(Player p, Skill skill) {
		deselect(p);
		if (selection.containsKey(skill))
			selection.get(skill).add(p);
		else System.out.println(ChatColor.RED + "[LS] Exception occured at "
				+ "skill selection, \"" + skill.toString() +"\" is not allowed (change it in config.yml)" );
	}
	
	public static void deselect(Player p) {
		for (Skill s : getSkills())
			if (selection.get(s).contains(p)) {
				selection.get(s).remove(p);
				break;
			}
	}
	
	public static boolean empty(Skill s) {
		return getSet(s).isEmpty();
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