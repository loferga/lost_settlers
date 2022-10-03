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
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.dogs.Anger;
import fr.loferga.lost_settlers.dogs.ComeBack;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.settings.MapSettings;
import fr.loferga.lost_settlers.rules.Wounded;
import fr.loferga.lost_settlers.skills.SkillRules;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Game extends BukkitRunnable {
	
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
		
		this.ms = MapMngr.getMapSettings(world);
		if (ms.canHostGame()) {
		
			Plugin plg = Main.getPlugin(Main.class);
			Anger.getInstance().start(plg);
			ComeBack.getInstance().start(plg);
			Wounded.getInstance().start(plg);
			SkillRules.getInstance().start(plg);
		}
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
		if (ms.isLodesActive()) {
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
	
	public Set<Player> getAliveEnnemies(Player p) {
		Set<Player> ennemies = new HashSet<>();
		for (Set<Player> pset : teams)
			if (!pset.contains(p))
				for (Player ennemy : pset)
					if (ennemy.isOnline() && ennemy.getGameMode() != GameMode.SPECTATOR)
						ennemies.add(ennemy);
		return ennemies;
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
	
	// MAGMA CHAMBER
	
	private Set<Player> inChamber = new HashSet<>();
	private boolean froze = false;
	
	public void addPlayerInChamber(Player p) {
		inChamber.add(p);
	}
	
	public boolean isInChamber(Location loc) {
		return loc.getY() < ms.chamberHeight;
	}
	
	public double undergroundLevel(Location loc) {
		return loc.getY() / ms.highestGround;
	}
	
	final int FORTY_FIVE_SECS = 900;
	final int ONE_MIN = 1200;
	final int SEVEN_MINS = 8400;
	final int EIGHT_MINS = 9600;
	
	private void chamber(int chrono) {
		if (pvp) {
			int conv = chrono % EIGHT_MINS;
			if (conv == SEVEN_MINS) {
				for (Player p : getPlayers())
					p.sendMessage(Func.format("&6La chambre magmatique commence à se refroidir"));
				if (lava.isEmpty())
					getChamberLava();
			}
			else if (conv > SEVEN_MINS)
				freezeRandomLava((int) (0.015*lava.size()));
			if (conv == 0)
				freezeChamber();
			if (froze) {
				if (conv == FORTY_FIVE_SECS)
					for (Player p : getPlayers())
						p.sendMessage(Func.format("&6La chambre magmatique commence à se rechauffer"));
				else if (conv > FORTY_FIVE_SECS) {
					unfreezeRandomLava((int) (0.07*lava.size()));
				}
				if (conv >= ONE_MIN)
					unfreezeChamber();
			}
		}
		if (froze) return;
		for (Player p : inChamber) {
			if (!isInChamber(p.getLocation()))
				inChamber.remove(p);
			if (p.getGameMode() == GameMode.SURVIVAL && p.getFireTicks() <= 0)
				p.setFireTicks(40);
		}
	}
	
	private List<Location> lava = new ArrayList<>();
	private List<Location> lava_cache = new ArrayList<>();
	
	private void getChamberLava() {
		Location center = MapMngr.getMapCenter(world, ms);
		double xm = center.getX() + ms.playableArea;
		double ym = ms.chamberHeight;
		double zm = center.getZ() + ms.playableArea;
		for (double x = center.getX() - ms.playableArea; x < xm; x+=1)
			for (double y = 3; y < ym; y+=1)
				for (double z = center.getZ() - ms.playableArea; z < zm; z+=1) {
					Location bloc = new Location(world, x, y, z);
					if (bloc.getBlock().getType() == Material.LAVA)
						lava.add(bloc);
				}
		lava_cache = new ArrayList<Location>(lava);
	}
	
	private void freezeChamber() {
		if (froze) return;
		for (Player p : getPlayers())
			p.sendMessage(Func.format("&cLa chambre magmatique s'est refroidie!"));
		for (Location loc : lava) {
			Block b = loc.getBlock();
			if (b.getType() == Material.LAVA)
				b.setType(Material.OBSIDIAN);
		}
		lava_cache = new ArrayList<Location>(lava);
		froze = true;
	}
	
	public void unfreezeChamber() {
		if (!froze) return;
		for (Player p : getPlayers())
			p.sendMessage(Func.format("&cLa chambre magmatique s'est réchauffee!"));
		for (Location loc : lava) {
			Block b = loc.getBlock();
			if (b.getType() == Material.OBSIDIAN)
				b.setType(Material.LAVA);
		}
		lava_cache = new ArrayList<Location>(lava);
		froze = false;
	}
	
	private void changeRandomLava(Material mat) {
		int size = lava_cache.size();
		if (size <= 0) return;
		int ir = ThreadLocalRandom.current().nextInt(size);
		lava_cache.get(ir).getBlock().setType(mat);
		lava_cache.remove(ir);
	}
	
	private void freezeRandomLava(int count) {
		for (int i = 0; i < count; i++)
			changeRandomLava(Material.OBSIDIAN);
	}
	
	private void unfreezeRandomLava(int count) {
		for (int i = 0; i < count; i++)
			changeRandomLava(Material.LAVA);
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
		if (pvp)
			conquest();
		if (chrono % 20 == 0) { respawn(); if (ms.isChamberActive()) chamber(chrono); }
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
		GameMngr.remove(this);
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
	
	public void kill(Player v, Player k) {
		LSTeam kt = getTeam(k);
		// add memory
		kMem.get(TeamMngr.indexOf(kt)).add(v);
		// revive
		LSTeam vt = TeamMngr.teamOf(v);
		int vti = TeamMngr.indexOf(vt);
		Set<Player> vm = kMem.get(vti);
		if (vm != null) 
			for (Player p : vm)
				if (getTeam(p) == kt) {
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
		winCondition(kt, v);
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
		winCondition(t, null);
	}
	
	public void winCondition(LSTeam t, Player killed) {
		if (t != null)
			winTest(t, killed);
		else
			for (int i = 0; i<teams.size(); i++)
				winTest(TeamMngr.get()[i], killed);
	}
	
	private void winTest(LSTeam t, Player killed) {
		if (allCampsBelongTo(t) || noOtherTeamAlive(t, killed)) {
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
	
	private boolean noOtherTeamAlive(LSTeam t, Player killed) {
		LSTeam[] teams = TeamMngr.get();
		for (int i = 0; i<teams.length; i++)
			if (teams[i] != t) {
				Set<Player> pset = TeamMngr.alivePlayers(this.teams.get(i));
				pset.remove(killed);
				if (pset.size() > 0)
					return false;
			}
		return true;
	}
	
	public boolean teamProtect(LSTeam t, Camp c) {
		for (Player p : TeamMngr.alivePlayers(teams.get(TeamMngr.indexOf(t))))
			if (inPerimeter(p.getLocation(), ms.vitalSize) != null)
				return true;
		return false;
	}
	
	public Camp campIn(Location loc) {
		return inPerimeter(loc, ms.campSize);
	}
	
	public Camp vitalIn(Location loc) {
		return inPerimeter(loc, ms.vitalSize);
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
	public boolean isFlag(Block b) {
		boolean isFlag = false;
		Location loc = b.getLocation().add(0.5, 0.5, 0.5);
		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
		for (Camp c : ms.camps) {
			double[] cp = c.getPosition();
			if (isNearBy(x, cp[0], 0.5) && isNearBy(y, cp[1] + 6, 5.5) && isNearBy(z, cp[2], 0.5))
				isFlag = true;
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