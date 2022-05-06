package fr.loferga.lost_settlers.map.camps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.scoreboard.Team;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;

public class Camp {
	
	private class Owner {
		
		Owner(Team t, long l) {
			team = t;
			time = l;
		}
		
		private Team team;
		private long time;
		
		public Team getTeam() {
			return team;
		}
		
		public long getTime() {
			return time;
		}
		
	} 
	
	Camp(String name, Team team, double[] pos, boolean dir) {
		this.name = name;
		this.owners = new ArrayList<>(Arrays.asList(new Owner(team, System.currentTimeMillis())));
		this.rivals = new ArrayList<>(Arrays.asList(team));
		this.pos = pos;
		this.dir = dir;
	}
	
	private String name;
	private List<Owner> owners;
	private List<Team> rivals;
	private double[] pos;
	private boolean dir;
	
	public String getName() {
		return name;
	}
	
	public Team getOwner() {
		return owners.get(owners.size()-1).getTeam();
	}
	
	public List<Team> getRivals() {
		return rivals;
	}
	
	public double[] getPos() {
		return pos;
	}
	
	public boolean getDir() {
		return dir;
	}
	
	public boolean isOwner(Team team) {
		return getOwner() == team;
	}
	
	public void setOwner(Team team) {
		owners.add(new Owner(team, System.currentTimeMillis()));
		rivals = new ArrayList<>(Arrays.asList(team));
	}
	
	public void addRival(Team team) {
		rivals.add(team);
	}
	
	public void removeRival(Team team) {
		rivals.remove(team);
	}
	
	public Location getLoc() {
		return Func.getPosLoc(Main.map, pos);
	}
	
	public long getOwnerTime() {
		return owners.get(owners.size()-1).getTime();
	}
	
}
