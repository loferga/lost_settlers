package fr.loferga.lost_settlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.dogs.Anger;
import fr.loferga.lost_settlers.dogs.ComeBack;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.map.MagmaChamber;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.MapSettings;
import fr.loferga.lost_settlers.map.Tombstone;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.rules.CombatTracker;
import fr.loferga.lost_settlers.rules.Conquest;
import fr.loferga.lost_settlers.rules.Respawn;
import fr.loferga.lost_settlers.rules.Wounded;
import fr.loferga.lost_settlers.skills.SkillRules;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.Func;

public class Game extends BukkitRunnable {
	
	private long startTime;
	
	private World world;
	private MapSettings ms;
	private List<Tombstone> tombstones = new ArrayList<>();
	private Map<LSTeam, Set<Player>> teams;
	private Camp[] camps;
	private boolean pvp;
	
	// TASKS
	private Conquest conquest = new Conquest(this);
	private Respawn respawn = new Respawn(this);
	private MagmaChamber chamber = null;
	
	public Game(World world, Set<Player> players) {
		
		this.world = world;
		
		this.ms = MapMngr.getMapSettings(world);
		
		Map<LSTeam, Set<Player>> res = new HashMap<>();
		LSTeam[] allTeams = TeamMngr.get();
		for (int i = 0; i<ms.teamNumber; i++)
			res.put(allTeams[i], new HashSet<>());
		for (Player p : players)
			res.get(TeamMngr.teamOf(p)).add(p);
		this.teams = res;
		
		camps = ms.camps.clone();
		
		if (ms.isChamberActive())
			chamber = new MagmaChamber(this, ms.chamberHeight);
		
		// Singleton Threads init
		Anger.getInstance().start();
		ComeBack.getInstance().start();
		CombatTracker.getInstance().start();
		Wounded.getInstance().start();
		SkillRules.getInstance().start();
		
	}
	
	public long getStartTime() {
		return startTime;
	}
	
	public World getWorld() {
		return world;
	}
	
	public boolean isTombstones(Block b) {
		if (b.getType() == Material.SKELETON_SKULL) {
			for (int i = 0; i<tombstones.size(); i++) {
				Tombstone tomb = tombstones.get(i);
				if (tomb.isTombstone(b)) {
					tomb.drop();
					tombstones.remove(i);
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean pvp() {
		return pvp;
	}
	
	public boolean isChamberActive() {
		return chamber != null;
	}
	
	public boolean isChamberFrozen() {
		return isChamberActive() && chamber.isFrozen();
	}
	
	public boolean isInChamber(Location loc) {
		return isChamberActive() && chamber.isIn(loc);
	}
	
	public void addInChamber(Player p) {
		if (!isChamberActive()) return;
		
		chamber.add(p);
	}
	
	public void start() {
		startTime = System.currentTimeMillis();
		runTaskTimer(Main.plg(), 0L, 1L);
		respawn.runTaskTimer(Main.plg(), 0L, 20L);
		if (chamber != null) chamber.runTaskTimer(Main.plg(), 0L, 20L);
	}
	
	private static final int PVP_TIME = Main.plg().getConfig().getInt("pvp_time") * 20;
	private static final int HALF = PVP_TIME/2;
	private static final int FIFTH = PVP_TIME - PVP_TIME/5;
	private static final int FIFTEENTH = PVP_TIME - PVP_TIME/15;
	
	private static final int[] GOALS = new int[] {HALF, FIFTH, FIFTEENTH, PVP_TIME};
	private int chrono = 0;
	private int g = 0;
	
	@Override
	public void run() {
		if (g == GOALS.length) return;
		
		if (chrono >= GOALS[g]) {
			if (g == GOALS.length-1) {
				for (Set<Player> team : teams.values())
					for (Player p : team) {
						p.sendMessage(Func.format(Main.MSG_WARNING + "Captures et Combats Actives"));
						p.playSound(world.getSpawnLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, SoundCategory.MASTER, 400.0f, 1.0f);
					}
				pvp = true;
			} else {
				String[] rt = Func.toReadableTime((long) (Math.abs(GOALS[g] - PVP_TIME)) * 50);
				broadcastPlayers(Func.format(Main.MSG_ANNOUNCE + "Il reste &3" + rt[0] + rt[1] + rt[2]
						+ Main.MSG_ANNOUNCE+ " avant le debut des captures"));
			}
			g++;
		}
		chrono++;
	}
	
	public Long stop() {
		cancel();
		if (chamber != null) chamber.cancel();
		GameMngr.remove(this);
		return startTime;
	}
	
	/*
	 * ===============================================
	 *                     TEAMS
	 * ===============================================
	 */
	
	public Set<Player> getPlayers() {
		// explicit
		Set<Player> players = new HashSet<>();
		for (Set<Player> pset : teams.values())
			players.addAll(pset);
		return players;
	}
	
	// return the NULL team if p is a spectator and null if the player isn't related to the game, p's team otherwise
	public LSTeam getTeam(Player p)  {
		if (p.getWorld() != world) return null;
		
		for (Entry<LSTeam, Set<Player>> entry : teams.entrySet())
			if (entry.getValue().contains(p))
				return entry.getKey();
		return TeamMngr.NULL;
	}
	
	public Set<Player> getTeamMembers(LSTeam t) {
		return teams.get(t);
	}
	
	public List<Player> getAliveMembers(LSTeam t) {
		// explicit
		List<Player> players = new ArrayList<>();
		for (Player p : teams.get(t))
			if (p.isOnline() && !p.isDead() && p.isValid())
				players.add(p);
		return players;
	}
	
	public List<Player> getTeamMates(Player p) {
		List<Player> teamMates = getAliveMembers(getTeam(p));
		teamMates.remove(p);
		return teamMates;
	}
	
	public Set<Player> getAliveEnnemies(LSTeam t) {
		Set<Player> ennemies = new HashSet<>();
		if (t == null || t == TeamMngr.NULL) return new HashSet<>();
		
		for (Entry<LSTeam, Set<Player>> entry : teams.entrySet())
			if (entry.getKey() != t)
				ennemies.addAll(entry.getValue());
		return ennemies;
	}
	
	public Set<Player> getAliveEnnemies(Player p) {
		return getAliveEnnemies(getTeam(p));
	}
	
	public boolean teamProtect(LSTeam t, Camp c) {
		// exist at least one t members that is within c vitalZone
		for (Player p : getAliveMembers(t)) 
			if (inVital(p.getLocation(), c))
				return true;
		return false;
	}
	
	/*
	 * ===============================================
	 *                    CAPTURE
	 * ===============================================
	 */

	public void capture(Camp c, LSTeam t) {
		if (c.getOwner() != t) {
			if (c.getRivals().contains(t)) {
				conquestMessage(c.getOwner(), t);
			} else
				stealMessage(c.getOwner(), t);
			c.getLocation().getWorld().playSound(c.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 4f*ms.playableArea, 1.0f);
		}
		for (Player p : teams.get(t))
			p.playSound(c.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
		c.setOwner(t);
		c.modifyFlag(t.getFlag());
		respawn.campCapturedBy(t);
		winCondition(t);
	}
	
	public void conquest(Camp c, LSTeam t) {
		conquest.addConquest(c, t);
		c.modifyFlag(Material.GRAY_CONCRETE);
	}
	
	public Camp getOlderCamp(LSTeam t) {
		// within all camps owned by t it get the older one (the greater time since time of capture)
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
	
	public Camp campIn(Location loc) {
		for (Camp c : camps)
			if (inCamp(loc, c))
				return c;
		return null;
	}
	
	public Camp vitalIn(Location loc) {
		for (Camp c : camps) 
			if (inVital(loc, c))
				return c;
		return null;
	}
	
	private boolean inCamp(Location loc, Camp c) {
		return inPerimeter(loc, c, ms.campSize + 0.5);
	}
	
	private boolean inVital(Location loc, Camp c) {
		return inPerimeter(loc, c, ms.vitalSize + 0.5);
	}
	
	private boolean inPerimeter(Location loc, Camp c, double p) {
		double[] pos = c.getPosition();
		return loc.getWorld() == world
				&& Func.isNearBy(loc.getX(), pos[0], p) && Func.isNearBy(loc.getZ(), pos[1], p);
	}
	
	public void campLeavingCheck(Player p, Location from, Location to) {
		Camp cFrom = vitalIn(from);
		Camp cTo = vitalIn(to);
		if (cFrom == null && cTo != null) conquest.playerGetOffVital(p, cTo);
	}
	
	public boolean isFlag(Block b) {
		for (Camp c : camps)
			if (c.isFlag(b))
				return true;
		return false;
	}
	
	/*
	 * ===============================================
	 *                     DEATH
	 * ===============================================
	 */
	
	public void suicide(PlayerDeathEvent e) {
		tombstones.add(new Tombstone(e));
		respawn.respawn(e.getEntity(), getTeam(e.getEntity()));
	}
	
	public void kill(Player v, Player k) {
		LSTeam kt = TeamMngr.teamOf(k);
		respawn.addKill(kt, v);
		LSTeam vt = TeamMngr.teamOf(v);
		respawn.respawnKilled(kt, vt);
		if (getAliveMembers(vt).isEmpty()) {
			world.playSound(MapMngr.getMapCenter(world, ms), Sound.ENTITY_WITHER_SPAWN, ms.playableArea, 1.0f);
			broadcastPlayers(Func.format(Main.MSG_ANNOUNCE + "Les " + vt.getName() + Main.MSG_ANNOUNCE + " ont été tués par les " + kt.getName()));
			respawn.teamKilled(vt);
		}
		conquest.playerDie(v);
		v.setGameMode(GameMode.SPECTATOR);
		winCondition(kt);
	}
	
	/*
	 * ===============================================
	 *                  WIN CONDITION
	 * ===============================================
	 */
	
	public void winCondition() {
		for (LSTeam t : teams.keySet())
			winCondition(t);
	}
	
	public void winCondition(LSTeam t) {
		if (allCampsBelongTo(t) || noOtherTeamAlive(t)) {
			GameMngr.stop(this, t);
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
		for (LSTeam team : teams.keySet())
			if (team != t
			&& !getAliveMembers(team).isEmpty())
					return false;
		return true;
	}
	
	/*
	 * ===============================================
	 *                      MAP
	 * ===============================================
	 */
	
	public void buildMap() {
		System.out.println(Main.LOG_PREFIX + "flags placing ...");
		initFlags();
		MapMngr.setWorldBorder(world, ms);
		if (ms.isLodesActive()) {
			System.out.println(Main.LOG_PREFIX + "lodes generation ... ");
			MapMngr.generateLodes(world, ms);
		}
	}
	
	public void initFlags() {
		assignRandomTeams();
		for (Camp c : camps)
			c.placeFlag();
	}
	
	private void assignRandomTeams() {
		List<LSTeam> teamList = new ArrayList<>(teams.keySet());
		int rng;
		for (int i = 0; i<teamList.size(); i++) {
			rng = Func.randomInt(0, teamList.size());
			camps[i].setOwner(teamList.get(rng));
			teamList.remove(rng);
		}
	}
	
	public void clearFlags() {
		for (Camp c : camps) {
			c.killZoneEffect();
			c.modifyFlag(Material.WHITE_CONCRETE);
		}
	}
	
	/*
	 * ===============================================
	 *                      UTIL
	 * ===============================================
	 */
	
	public void broadcastPlayers(String msg) {
		for (Set<Player> team : teams.values())
			for (Player p : team)
				p.sendMessage(msg);
	}

	public void conquestMessage(LSTeam from, LSTeam to) {
		String msgFrom = Func.format(Main.MSG_ANNOUNCE + "Les " + to.getName() + Main.MSG_ANNOUNCE + " ont conquis votre camp!");
		String msgTo = Func.format(Main.MSG_ANNOUNCE + "Votre équipe a conquis le camp des " + from.getName());
		String msgDefault = Func.format(Main.MSG_ANNOUNCE + "Le camp des " + from.getName() + Main.MSG_ANNOUNCE + " a été conquis par les " + to.getName());
		for (Player p : getPlayers()) {
			LSTeam pteam = TeamMngr.teamOf(p);
			if (pteam == from) p.sendMessage(msgFrom);
			else if (pteam == to) p.sendMessage(msgTo);
			else p.sendMessage(msgDefault);
		}
	}
	
	public void stealMessage(LSTeam from, LSTeam to) {
		String msgFrom = Func.format(Main.MSG_ANNOUNCE + "Les " + to.getName() + Main.MSG_ANNOUNCE + " ont volés votre camp!");
		String msgTo = Func.format(Main.MSG_ANNOUNCE + "Votre équipe a volé le camp des " + from.getName());
		String msgDefault = Func.format(Main.MSG_ANNOUNCE + "Le camp des " + from.getName() + Main.MSG_ANNOUNCE + " a été volé par les " + to.getName());
		for (Set<Player> team : teams.values())
			for (Player p : team) {
				LSTeam pteam = TeamMngr.teamOf(p);
				if (pteam == from) p.sendMessage(msgFrom);
				else if (pteam == to) p.sendMessage(msgTo);
				else p.sendMessage(msgDefault);
			}
	}
	
}