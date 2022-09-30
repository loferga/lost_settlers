package fr.loferga.lost_settlers.map.camps;

import fr.loferga.lost_settlers.teams.LSTeam;

public class Owner {
	
	public Owner(LSTeam t, long l) {
		team = t;
		time = l;
	}
	
	private LSTeam team;
	private long time;
	
	public LSTeam getTeam() {
		return team;
	}
	
	public long getTime() {
		return time;
	}
	
}