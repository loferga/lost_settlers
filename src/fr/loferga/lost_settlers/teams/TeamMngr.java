package fr.loferga.lost_settlers.teams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.dogs.DogsMngr;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.camps.CampMngr;

public class TeamMngr {
	
	private static ConfigurationSection section = Main.getPlugin(Main.class).getConfig().getConfigurationSection("teams");
	private static Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
	
	private static final List<Team> TEAMS = buildTeams(section, sb);
	private static Map<Team, Map<Team, List<Player>>> kMem = new HashMap<>();
	
	public static List<Team> get() {
		return new ArrayList<>(TEAMS);
	}
	
	@SuppressWarnings("deprecation")
	public static void join(Player p, Team team) {
		team.addPlayer(p);
		DogsMngr.setDogsColor(p, getDyeColor(p));
	}
	
	@SuppressWarnings("deprecation")
	public static void remove(Player p) {
		Team team = teamOf(p);
		if (team != null) team.removePlayer(p);
		DogsMngr.setDogsColor(p, DyeColor.WHITE);
	}
	
	@SuppressWarnings("deprecation")
	public static Team teamOf(Player p) {
		for (Team team : TEAMS)
			if (team.hasPlayer(p))
				return team;
		return null;
	}
	
	@SuppressWarnings("deprecation")
	public static List<Player> getPlayers(Team team) {
		List<Player> pset = new ArrayList<>();
		for (OfflinePlayer offp : team.getPlayers())
			if (offp.isOnline())
				pset.add((Player) offp);
		return pset;
	}
	
	private static void removeKilled(Team k, Team v, Player p) {
		if (kMem.get(k).get(v).size() == 1)
			if (kMem.get(k).size() == 1)
				kMem.remove(k);
			else
				kMem.get(k).remove(v);
		else
			kMem.get(k).get(v).remove(p);
	}
	
	private static boolean isKillerOf(Team k, Team v) {
		return kMem.containsKey(k) && kMem.get(k).containsKey(v);
	}
	
	/*
	 * ============================================================================
	 *                                TEAM INIT
	 * ============================================================================
	 */
	
	public static List<Team> buildTeams(ConfigurationSection section, Scoreboard sb) {
		List<Team> teams = new ArrayList<>();
		for (String key : section.getKeys(false)) {
			String keyinfo = section.getString(key);
			try {
				teams.add(createTeam(sb, Func.unformat(keyinfo.charAt(0)), keyinfo.substring(1)));
			} catch (Exception e) {
				teams.add(sb.getTeam(keyinfo.substring(1)));
			}
			
		}
		return teams;
	}
	
	private static Team createTeam(Scoreboard sb, ChatColor color, String name) {
		Team team = sb.registerNewTeam(name);
		team.setColor(color);
		team.setDisplayName(color + name);
		team.setCanSeeFriendlyInvisibles(false);
		team.setOption(Option.COLLISION_RULE, OptionStatus.NEVER);
		return team;
	}
	
	/*
	 * ============================================================================
	 *                           TEAM UTIL FUNCTIONS
	 * ============================================================================
	 */
	
	public static List<Player> getAliveTeamMembers(Team team) {
		List<Player> pset= new ArrayList<>();
		for (Player p : getPlayers(team))
			if (!p.isDead() && p.getGameMode() != GameMode.SPECTATOR)
				pset.add(p);
		return pset;
	}
	
	public static List<Player> getTeamedPlayers() {
		List<Player> pset = new ArrayList<>();
		for (Team team : TEAMS)
			pset.addAll(getPlayers(team));
		return pset;
	}
	
	// IMPROVE
	public static int[] getTeamsSize() {
		int i = 0;
		int[] teamSize = new int[3];
		String[] names = new String[] {"Oranges", "Rouges", "Violets"};
		while (i < names.length) {
			for (Team team : TEAMS)
				if (team.getName().equals(names[i]))
					teamSize[i] = getAliveTeamMembers(team).size();
			i++;
		}
		return teamSize;
	}
	
	public static void teamKills(Player dead, Player killer) {
		Team k = teamOf(killer);
		Team v = teamOf(dead);
		if (k != v) {
			// revive
			if (isKillerOf(v, k)) {
				if (CampMngr.getTeamCamps(v).size() > 0)
					respawnKilled(v, k);
			}
			// add to kMem
			if (!kMem.containsKey(k))
				kMem.put(k, new HashMap<>(Map.of(v, new ArrayList<>(Arrays.asList(dead)))));
			else if (!kMem.get(k).containsKey(v))
				kMem.get(k).put(v, new ArrayList<>(Arrays.asList(dead)));
			else
				kMem.get(k).get(v).add(dead);
		}
	}
	
	public static void respawnAllKilled(Team k) {
		if (kMem.containsKey(k))
			for (Team v : kMem.get(k).keySet())
				for (Player p : kMem.get(k).get(v)) {
					MapMngr.campTeleport(p, getOlderCamp(v));
					p.setGameMode(GameMode.SURVIVAL);
					removeKilled(k, v, p);
				}
	}
	
	public static void respawnKilled(Team k, Team v) {
		for (Player p : kMem.get(k).get(v)) {
			MapMngr.campTeleport(p, getOlderCamp(v));
			p.setGameMode(GameMode.SURVIVAL);
			removeKilled(k, v, p);
		}
	}
	
	public static void respawn(Player p) {
		MapMngr.campTeleport(p, getOlderCamp(teamOf(p)));
		p.setGameMode(GameMode.SURVIVAL);
	}
	
	public static Camp getOlderCamp(Team t) {
		Camp older = null;
		long time = Long.MAX_VALUE;
		for (Camp c : CampMngr.getTeamCamps(t)) {
			long ctime = c.getOwnerTime();
			if (ctime < time) {
				older = c;
				time = ctime;
			}
		}
		return older;
	}
	
	public static void resetKillerMemory() {
		kMem.clear();
	}
	
	public static DyeColor getDyeColor(Player p) {
		Team team = teamOf(p);
		if (team==null) return DyeColor.WHITE;
		switch(team.getColor()) {
		case GOLD: return DyeColor.ORANGE;
		case RED: return DyeColor.RED;
		default: return DyeColor.PURPLE;
		}
	}
	
	public static Color getColor(Player p) {
		Team team = teamOf(p);
		if (team==null) return Color.WHITE;
		switch(team.getColor()) {
		case GOLD: return Color.ORANGE;
		case RED: return Color.RED;
		default: return Color.PURPLE;
		}
	}
	
	public static Material getTeamMaterial(Team team) {
		switch(team.getColor()) {
		case GOLD: return Material.ORANGE_CONCRETE;
		case RED: return Material.RED_CONCRETE;
		case LIGHT_PURPLE: return Material.PURPLE_CONCRETE;
		default: return Material.WHITE_CONCRETE;
		}
	}
	
}
