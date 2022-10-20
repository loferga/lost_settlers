package fr.loferga.lost_settlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.MapSettings;
import fr.loferga.lost_settlers.map.Tombstone;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.rules.Respawn;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.Func;

public class GameV2 {
	
	private World world;
	private List<Tombstone> tombstones = new ArrayList<>();
	private Map<LSTeam, Set<Player>> teams;
	private Camp[] camps;
	private MapSettings ms;
	
	// TASKS
	private Respawn respawn = new Respawn(this);
	
	public GameV2(World world, Set<Player> players) {
		
		this.world = world;
		
		Map<LSTeam, Set<Player>> res = new HashMap<>();
		for (LSTeam t : TeamMngr.get())
			res.put(t, new HashSet<>());
		for (Player p : players)
			res.get(TeamMngr.teamOf(p)).add(p);
		this.teams = res;
		
		camps = ms.camps.clone();
		
		respawn.runTaskTimer(org.bukkit.plugin.java.JavaPlugin.getPlugin(Main.class), 0L, 20L);
		
	}
	
	public World getWorld() {
		return world;
	}
	
	public Set<Player> getPlayers() {
		Set<Player> players = new HashSet<>();
		for (Set<Player> pset : teams.values())
			players.addAll(pset);
		return players;
	}
	
	public void broadcastPlayers(String msg) {
		for (Set<Player> team : teams.values())
			for (Player p : team)
				p.sendMessage(msg);
	}
	
	public Set<Player> getAliveMembers(LSTeam t) {
		Set<Player> players = new HashSet<>();
		for (Player p : teams.get(t))
			if (p.isOnline() && !p.isDead() && p.isValid())
				players.add(p);
		return players;
	}
	
	public Camp getOlderCamp(LSTeam t) {
		Camp older = null;
		long time = Long.MAX_VALUE;
		for (Camp c : camps) {
			if (c.getOwner() == t) { 
				long ctime = c.getOwnerTime();
				if (ctime < time) {
					older = c;
					time = ctime;
				}
			}
		}
		return older;
	}
	
	public void suicide(PlayerDeathEvent e) {
		tombstones.add(new Tombstone(e));
		respawn.addRespawn(e.getEntity());
	}
	
	public void kill(Player v, Player k) {
		LSTeam kt = TeamMngr.teamOf(k);
		respawn.addKill(kt, v);
		LSTeam vt = TeamMngr.teamOf(v);
		respawn.respawnKilled(kt, vt);
		if (getAliveMembers(vt).isEmpty()) {
			world.playSound(MapMngr.getMapCenter(world, ms), Sound.ENTITY_WITHER_SPAWN, ms.playableArea, 1.0f);
			broadcastPlayers(Func.format("&eLes " + vt.getName() + "&e ont été tués par les " + kt.getName()));
			respawn.teamKilled(vt);
		}
		winCondition(kt);
	}
	
	/*
	 * ===============================================
	 *                    UTIL
	 * ===============================================
	 */
	
	public void winCondition() {
		for (LSTeam t : teams.keySet())
			winCondition(t);
	}
	
	public void winCondition(LSTeam t) {
		if (allCampsBelongTo(t) || noOtherTeamAlive(t)) {
			// GameMngr.stop(this, t);
			for (Player p : getPlayers())
				p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10.0f, 1.0f);
		}
	}
	
	private boolean allCampsBelongTo(LSTeam t) {
		for (Camp camp : ms.camps) {
			if (!camp.isOwner(t))
				return false;
		}
		return true;
	}
	
	private boolean noOtherTeamAlive(LSTeam t) {
		LSTeam[] teams = TeamMngr.get();
		for (LSTeam team : teams)
			if (team != t)
				if (getAliveMembers(team).size() > 0)
					return false;
		return true;
	}
	
}