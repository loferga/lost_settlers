package fr.loferga.lost_settlers.rules;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.Func;

public class Respawn extends BukkitRunnable {
	
	private Game game;
	private Set<Player> waitingForRespawn = new HashSet<>();
	private Map<LSTeam/*killerTeam*/, Set<Player>/*victims*/> killMemory = new HashMap<>();
	
	public Respawn(Game game) {
		this.game = game;
	}
	
	@Override
	public void run() {
		for (Player respawn : new HashSet<>(waitingForRespawn)) {
			LSTeam t = TeamMngr.teamOf(respawn);
			Camp c = game.getOlderCamp(t);
			if (c != null) {
				respawn(respawn, t);
				waitingForRespawn.remove(respawn);
			} else
				Func.sendActionbar(respawn, Func.format(Main.MSG_PERSONNAL + "Vous n'avez pas de camp"));
		}
	}

	public void addRespawn(Player p) {
		waitingForRespawn.add(p);
		Func.sendActionbar(p, Func.format(Main.MSG_PERSONNAL + "Vous n'avez pas de camp"));
	}
	
	public void campCapturedBy(LSTeam t) {
		for (Player p : new HashSet<>(waitingForRespawn))
			if (TeamMngr.teamOf(p) == t)
				respawn(p);
	}
	
	public void addKill(LSTeam kt, Player v) {
		if (killMemory.containsKey(kt))
			killMemory.put(kt, new HashSet<>(Set.of(v)));
		else
			killMemory.get(kt).add(v);
	}
	
	public void respawnKilled(LSTeam ker/*killer*/, LSTeam ked/*killed*/) {
		if (!killMemory.containsKey(ked)) return;
		
		Set<Player> vm = killMemory.get(ked);
		for (Player v : vm) {
			LSTeam vt = TeamMngr.teamOf(v);
			if (vt == ker)
				respawn(v, vt);
		}
		
	}
	
	public void teamKilled(LSTeam t) {
		for (Player p : waitingForRespawn)
			if (TeamMngr.teamOf(p) == t) {
				waitingForRespawn.remove(p);
				Func.sendActionbar(p, Func.format(Main.MSG_PERSONNAL + "Votre équipe à été vaincue"));
			}
	}

	public void respawn(Player p, Camp c) {
		MapMngr.campTeleport(p, c);
		c.getLocation().getWorld().playSound(c.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 10.0f, 1.0f);
		p.setGameMode(GameMode.SURVIVAL);
	}
	
	public void respawn(Player p, LSTeam t) {
		Camp c = game.getOlderCamp(t);
		if (c == null) addRespawn(p); 
		else respawn(p, game.getOlderCamp(t));
	}
	
	public void respawn(Player p) {
		respawn(p, TeamMngr.teamOf(p));
	}
	
}