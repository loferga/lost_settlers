package fr.loferga.lost_settlers.teams;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

public class LSTeam {
	
	public LSTeam(Team team, Material flag, DyeColor dyeColor, Color color) {
		this.team = team;
		this.flag = flag;
		this.dyeColor = dyeColor;
		this.color = color;
	}
	
	private Team team;
	private Material flag;
	private DyeColor dyeColor;
	private Color color;
	
	public Team getTeam() {
		return team;
	}
	
	public Material getFlag() {
		return flag;
	}
	
	public ChatColor getChatColor() {
		return team.getColor();
	}
	
	public DyeColor getDyeColor() {
		return dyeColor;
	}
	
	public Color getColor() {
		return color;
	}
	
	public String getRawName() {
		return team.getName();
	}
	
	public String getName() {
		return team.getDisplayName();
	}
	
	public void join(Player p) {
		team.addEntry(p.getName());
	}
	
	public void leave(Player p) {
		team.removeEntry(p.getName());
	}
	
	public Set<Player> getPlayers() {
		Set<Player> pset = new HashSet<>();
		for (String pname : team.getEntries()) {
			Player p = Bukkit.getPlayer(pname);
			if (p != null) pset.add(p);
		}
		return pset;
			
	}
	
	public boolean isMember(Player p) {
		return team.hasEntry(p.getName());
	}
	
	public void unregister() {
		team.unregister();
	}
	
	@Override
	public String toString() {
		return team.getName();
	}
	
}