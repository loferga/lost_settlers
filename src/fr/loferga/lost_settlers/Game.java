package fr.loferga.lost_settlers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.MapSettings;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.rules.RuleManager;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Game extends BukkitRunnable {
	
	private static RuleManager rules = null;
	
	private static int pvp_time = Main.getPlugin(Main.class).getConfig().getInt("pvp_time") * 20;
	private static int half = pvp_time/2;
	private static int fifth = pvp_time - pvp_time/5;
	private static int fifteenth = pvp_time - pvp_time/15;
	
	public Game(World world, Set<Player> players) {
		this.world = world;
		pvp = false;
		startT = System.currentTimeMillis();
		
		List<Set<Player>> res = new ArrayList<>();
		for (int i = 0; i<TeamMngr.get().length; i++)
			res.add(new HashSet<>());
		for (Player p : players)
			res.get(TeamMngr.indexOf(TeamMngr.teamOf(p))).add(p);
		this.teams = res;
		
		MapSettings ms = MapMngr.getMapSettings(world);
		if (ms == null)
			ms = new MapSettings(world, Main.getPlugin(Main.class).getConfig().getConfigurationSection("maps." + world.getName().substring(4)));
		this.ms = ms;
			
		
		if (GameMngr.noGames()) rules = new RuleManager();
	}
	
	private boolean pvp;
	private long startT;
	
	public boolean pvp() {
		return pvp;
	}
	
	public long getStartTime() {
		return startT;
	}
	
	private World world;
	private List<Set<Player>> teams;
	private MapSettings ms;
	
	public void buildMap() {
		System.out.println("[LS] flags placing ...");
		initFlags();
		MapMngr.setWorldBorder(world, ms);
		if (ms.lodes) {
			System.out.println("[LS] lodes generation ... ");
			MapMngr.generateLodes(world, ms);
		}
	}
	
	public World getWorld() {
		return world;
	}
	
	public Set<Player> getPlayers() {
		Set<Player> res = new HashSet<>();
		for (Set<Player> pl : teams)
			res.addAll(pl);
		return res;
	}
	
	public Set<Player> getMembers(LSTeam t) {
		return new HashSet<>(teams.get(TeamMngr.indexOf(t)));
	}
	
	public List<Player> getAliveTeamMates(Player p) {
		List<Player> teamMates = new ArrayList<>();
		for (Set<Player> pset : teams)
			if (pset.contains(p)) {
				for (Player tm : pset)
					if (tm.isOnline() && tm.getGameMode() != GameMode.SPECTATOR)
						teamMates.add(tm);
				break;
			}
		teamMates.remove(p);
		return teamMates;
	}
	
	public LSTeam getTeam(Player p) {
		for (int i = 0; i < teams.size(); i++)
			if (teams.get(i).contains(p))
				return TeamMngr.get()[i];
		return null;
	}
	
	public MapSettings getMapSettings() {
		return ms;
	}
	
	// CONQUEST
	
	private Set<Camp> disputedCamps = new HashSet<>();
	
	public void addConquest(Camp camp, LSTeam team) {
		disputedCamps.add(camp);
		camp.addRival(team);
	}
	
	private void conquest() {
		for (Camp camp : disputedCamps) {
			for (LSTeam team : new ArrayList<>(camp.getRivals())) {
				if (!teamProtect(team, camp)) {
					camp.removeRival(team);
					for (Player p : teams.get(TeamMngr.indexOf(team)))
						p.playSound(camp.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
				}
			}
			if (camp.getRivals().size() == 1) {
				capture(camp, camp.getRivals().get(0));
				disputedCamps.remove(camp);
			}
		}
	}
	
	// RESPAWN
	
	private Map<Player, Integer> T = new HashMap<>();
	private Map<Player, Integer> N = new HashMap<>();

	public void addRespawn(Player p) {
		if (!N.containsKey(p))
			N.put(p, 0);
		T.put(p, (int) (30 * Math.pow(2, N.get(p))));
		N.replace(p, N.get(p) + 1);
	}
	
	public void removeRespawn(Player p) {
		T.remove(p);
	}
	
	private void respawn() {
		for (Player p : T.keySet()) {
			if (T.get(p) <= 0) {
				LSTeam pt = TeamMngr.teamOf(p);
				if (getTeamCamps(pt).size() >= 1) {
					respawn(p, pt);
					removeRespawn(p);
				} else
					Func.sendActionbar(p, Func.format("&cVous n'avez pas de camp où réapparaitre"));
			} else {
				Func.sendActionbar(p, Func.format("&e" + T.get(p)));
				T.replace(p, T.get(p) - 1);
			}
		}
	}
	
	// RUNNABLE
	
	private int chrono = 0;
	private int[] goals = new int[] {half, fifth, fifteenth, pvp_time, Integer.MAX_VALUE};
	private int c = 0;
	
	@Override
	public void run() {
		if (pvp) {
			conquest();
		}
		if (chrono % 20 == 0) respawn();
		//DISPLAY
		if (chrono >= goals[c]) {
			if (c<3) {
				String[] rt = Func.toReadableTime((long) (Math.abs(goals[c] - pvp_time)) * 50);
				for (Player p : world.getPlayers())
					p.sendMessage(Func.format("&eIl reste &3" + rt[0] + rt[1] + rt[2] + "&e avant le debut des captures"));
			} else {
				for (Player p : world.getPlayers()) {
					p.sendMessage(Func.format("&4Captures et Combats Actives"));
					p.playSound(world.getSpawnLocation(), Sound.ENTITY_ELDER_GUARDIAN_AMBIENT, SoundCategory.MASTER, 400.0f, 1.0f);
				}
				pvp = true;
			}
			c++;
		}
		chrono++;
	}
	
	public void stop() {
		pvp = false;
		if (GameMngr.noGames()) rules.stop();
		cancel();
	}
	
	/*
	 * ============================================================================
	 *                                KILL MEMORY
	 * ============================================================================
	 */
	
	private Map<Integer/*killerTeamIndex*/, Set<Player>/*victims*/> kMem = blankKMem();
	private Map<Integer, Set<Player>> blankKMem() {
		Map<Integer, Set<Player>> res = new HashMap<>();
		int teamNb = TeamMngr.get().length;
		for (int i = 0; i<teamNb; i++)
			res.put(i, new HashSet<>());
		return res;
	}
	
	public void kill(Player k, Player v) {
		LSTeam kt = TeamMngr.teamOf(k);
		// add memory
		kMem.get(TeamMngr.indexOf(kt)).add(v);
		// revive
		LSTeam vt = TeamMngr.teamOf(v);
		int vti = TeamMngr.indexOf(vt);
		Set<Player> vm = kMem.get(vti);
		if (vm != null) 
			for (Player p : vm)
				if (TeamMngr.teamOf(p) == kt) {
					respawn(p, kt);
					world.playSound(p.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 10.0f, 0.2f);
					p.sendMessage(Func.format(k.getDisplayName() + "&e vous a vengé!"));
					vm.remove(p);
				}
		if (getAliveTeamMates(v).size() == 0) {
			world.playSound(MapMngr.getMapCenter(world, ms), Sound.ENTITY_WITHER_SPAWN, 4*ms.playableArea, 1.0f);
			for (Player p : world.getPlayers())
				p.sendMessage(Func.format("&eLes " + vt.getName() + "&e ont été éliminés par les " + kt.getName()));
		}
		winCondition(kt);
	}

	public void respawn(Player p, LSTeam pTeam) {
		LSTeam pt = pTeam;
		if (pt == null) pt = TeamMngr.teamOf(p);
		MapMngr.campTeleport(p, getOlderCamp(pt));
		p.setGameMode(GameMode.SURVIVAL);
	}
	
	public Camp getOlderCamp(LSTeam t) {
		Camp older = null;
		long time = Long.MAX_VALUE;
		for (Camp c : getTeamCamps(t)) {
			long ctime = c.getOwnerTime();
			if (ctime < time) {
				older = c;
				time = ctime;
			}
		}
		return older;
	}
	
	/*
	 * ============================================================================
	 *                                    UTIL
	 * ============================================================================
	 */
	
	public void capture(Camp c, LSTeam t) {
		if (c.getOwner() != t) {
			if (c.getRivals().contains(t)) {
				conquestMessage(c.getOwner(), t);
			} else
				stealMessage(c.getOwner(), t);
			c.getLocation().getWorld().playSound(c.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 4*ms.playableArea, 1.0f);
		}
		for (Player p : teams.get(TeamMngr.indexOf(t)))
			p.playSound(c.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
		c.setOwner(t);
		c.placeFlag(t.getFlag());
		winCondition(t);
	}
	
	public void winCondition(LSTeam t) {
		if (t != null) {
			if (allCampsBelongTo(t) || noOtherTeamAlive(t)) {
				GameMngr.stop(this, t);
				for (Player p : getPlayers())
					p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 10.0f, 1.0f);
			}
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
		for (int i = 0; i<teams.length; i++)
			if (teams[i] != t)
				if (TeamMngr.alivePlayers(this.teams.get(i)).size() > 0)
					return false;
		return true;
	}
	
	public boolean teamProtect(LSTeam t, Camp c) {
		for (Player p : TeamMngr.alivePlayers(teams.get(TeamMngr.indexOf(t))))
			if (inPerimeter(p.getLocation(), ms.vSize) != null)
				return true;
		return false;
	}
	
	public Camp campIn(Location loc) {
		return inPerimeter(loc, ms.cSize);
	}
	
	public Camp vitalIn(Location loc) {
		return inPerimeter(loc, ms.vSize);
	}
	
	private Camp inPerimeter(Location loc, double P) {
		if (loc.getWorld() == world) {
			for (Camp c : ms.camps) {
				double[] pos = c.getPosition();
				if (isNearBy(loc.getX(), pos[0], P) && isNearBy(loc.getZ(), pos[2], P))
					return c;
			}
		}
		return null;
	}

	public List<Camp> getTeamCamps(LSTeam t) {
		List<Camp> res = new ArrayList<>();
		for (Camp camp : ms.camps)
			if (camp.isOwner(t))
				res.add(camp);
		return res;
	}
	
	public Set<Player> aliveMembers(LSTeam team) {
		Set<Player> pset = new HashSet<>();
		for (Player p : teams.get(TeamMngr.indexOf(team)))
			if (!p.isDead() && p.getGameMode() != GameMode.SPECTATOR)
				pset.add(p);
		return pset;
	}
	public void conquest(Camp c, LSTeam t) {
		addConquest(c, t);
		c.placeFlag(Material.GRAY_CONCRETE);
	}

	public void initFlags() {
		assignRandomTeams();
		for (Camp c : ms.camps)
			c.placeFlag(c.getOwner().getFlag());
	}
	
	private void assignRandomTeams() {
		LSTeam[] teams = TeamMngr.get().clone();
		Camp[] camps = ms.camps;
		for (int i = 0; i<camps.length; i++) {
			int rng = ThreadLocalRandom.current().nextInt(teams.length-i);
			camps[i].setOwner(teams[rng]);
			int last = teams.length-1-i;
			LSTeam tmp = teams[rng];
			teams[rng] = teams[last];
			teams[last] = tmp;
		}
	}
	
	public void clearFlags() {
		for (Camp c : ms.camps)
			c.placeFlag(Material.WHITE_CONCRETE);
	}
	
	// is there a flag where the location point
	public boolean isFlag(Location loc) {
		boolean isFlag = false;
		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
		for (Camp c : ms.camps) {
			double[] cp = c.getPosition();
			if (isNearBy(x, cp[0], 0.5) && isNearBy(y, cp[1] + 6, 5.5) && isNearBy(z, cp[2], 0.5)) {
				isFlag = true;
			}
			if (isNearBy(y, cp[1] + 9.5, 1.5)) {
				if (c.getDirection()) {
					if (isNearBy(x, cp[0], 0.5) && isNearBy(z, cp[2], 1.5))
						isFlag = true;
				} else {
					if (isNearBy(x, cp[0], 1.5) && isNearBy(z, cp[2], 0.5))
						isFlag = true;
				}
			}
		}
		return isFlag;
	}
	
	// square shaped distance check
	public static boolean isNearBy(double pos, double around, double by) {
		return around - by <= pos && pos < around + by;
	}
	
	/*
	 * ============================================================================
	 *                                  DISPLAY
	 * ============================================================================
	 */
	
	public void conquestMessage(LSTeam from, LSTeam to) {
		String msgFrom = Func.format("&eLes " + to.getName() + "&e ont conquis votre camp!");
		String msgTo = Func.format("&eVotre équipe a conquis le camp des " + from.getName());
		String msgDefault = Func.format("&eLe camp des " + from.getName() + "&e a été conquis par les " + to.getName());
		for (Player p : getPlayers()) {
			LSTeam pteam = TeamMngr.teamOf(p);
			if (pteam == from) p.sendMessage(msgFrom);
			else if (pteam == to) p.sendMessage(msgTo);
			else p.sendMessage(msgDefault);
		}
	}
	
	public void stealMessage(LSTeam from, LSTeam to) {
		String msgFrom = Func.format("&eLes " + to.getName() + "&e ont volés votre camp!");
		String msgTo = Func.format("&eVotre équipe a volé le camp des " + from.getName());
		String msgDefault = Func.format("&eLe camp des " + from.getName() + "&e a été volé par les " + to.getName());
		for (Player p : getPlayers()) {
			LSTeam pteam = TeamMngr.teamOf(p);
			if (pteam == from) p.sendMessage(msgFrom);
			else if (pteam == to) p.sendMessage(msgTo);
			else p.sendMessage(msgDefault);
		}
	}
	
}