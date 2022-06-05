package fr.loferga.lost_settlers.teams;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.dogs.DogsMngr;

public class TeamMngr {
	
	private static ConfigurationSection section = Main.getPlugin(Main.class).getConfig().getConfigurationSection("teams");
	private static Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
	
	private static final LSTeam[] TEAMS = buildTeams(section, sb);
	
	public static LSTeam[] get() {
		return TEAMS;
	}
	
	public static void join(Player p, LSTeam team) {
		team.join(p);
		DogsMngr.setDogsColor(p, team.getDyeColor());
	}
	
	public static void remove(Player p) {
		LSTeam team = teamOf(p);
		if (team != null) team.leave(p);
		DogsMngr.setDogsColor(p, DyeColor.WHITE);
	}
	
	public static LSTeam teamOf(Player p) {
		for (LSTeam team : TEAMS)
			if (team.isMember(p))
				return team;
		return null;
	}
	
	public static int indexOf(LSTeam t) {
		boolean found = false;
		int i = TEAMS.length-1;
		while(i>=0 && !found) {
			if (TEAMS[i] == t)
				found = true;
			else
				i--;
		}
		return i;
	}
	
	/*
	 * ============================================================================
	 *                                TEAM INIT
	 * ============================================================================
	 */
	
	public static LSTeam[] buildTeams(ConfigurationSection section, Scoreboard sb) {
		colors = new Color[] {
				Color.AQUA, Color.BLACK, Color.BLUE, Color.FUCHSIA, Color.GRAY,
				Color.GREEN, Color.LIME, Color.MAROON,Color.NAVY, Color.OLIVE,
				Color.ORANGE, Color.PURPLE, Color.RED, Color.SILVER, Color.TEAL,
				Color.WHITE, Color.YELLOW
		};
		LSTeam[] teams = new LSTeam[section.getKeys(false).size()];
		int i = 0;
		for (String key : section.getKeys(false)) {
			List<String> data = section.getStringList(key);
			Team team = null;
			try {
				team = createTeam(sb, ChatColor.valueOf(data.get(0).toUpperCase()), key);
			} catch (Exception e) {
				team = sb.getTeam(key);
			}
			teams[i] = new LSTeam(team, Material.valueOf(data.get(1).toUpperCase()), DyeColor.valueOf(data.get(2).toUpperCase()), colorValueOf(data.get(3).toUpperCase()));
			i++;
		}
		return teams;
	}
	
	private static Team createTeam(Scoreboard sb, ChatColor color, String name) {
		Team team = sb.registerNewTeam(name);
		team.setColor(color);
		team.setDisplayName(color + name);
		team.setCanSeeFriendlyInvisibles(false);
		team.setOption(Option.COLLISION_RULE, OptionStatus.ALWAYS);
		return team;
	}
	
	private static Color[] colors;
	
	private static Color colorValueOf(String str) {
		for (Color clr : colors)
			if (clr.toString().equals(str))
				return clr;
		return null;
	}
	
	/*
	 * ============================================================================
	 *                                    UTIL
	 * ============================================================================
	 */
	
	public static Set<Player> alivePlayers(Set<Player> pset) {
		Set<Player> res = new HashSet<>();
		for (Player p : pset)
			if (p.isOnline() && p.getGameMode() != GameMode.SPECTATOR)
				res.add(p);
		return res;
	}
	
	public static Set<Player> teamedPlayers(World world) {
		Set<Player> pset = new HashSet<>();
		for (LSTeam team : TEAMS)
			for (Player p : team.getPlayers())
				if (p.getWorld() == world)
					pset.add(p);
		return pset;
	}
	
	// IMPROVE
	public static int[] teamsSizes(World world) {
		int[] teamSize = new int[] {0, 0, 0};
		for (int i = 0; i<TEAMS.length; i++) {
			for (Player p : TEAMS[i].getPlayers())
				if (p.getWorld() == world)
					teamSize[i]++;
		}
		return teamSize;
	}
	
}
