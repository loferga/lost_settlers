package fr.loferga.lost_settlers.map.camps;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.game.EndGameMngr;
import fr.loferga.lost_settlers.tasks.Conquest;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class CampMngr {
	
	private static List<Camp> camps = new ArrayList<>();
	private static double size;
	private static double ratio;
	
	public static List<Camp> get() {
		return new ArrayList<>(camps);
	}
	
	/*
	 * ============================================================================
	 *                                    INIT
	 * ============================================================================
	 */
	
	public static void buildCamps(ConfigurationSection cfg) {
		camps.clear();
		List<Team> teams = TeamMngr.get();
		Set<String> keys = cfg.getConfigurationSection("positions").getKeys(false);
		if (teams.size() >= keys.size())
			for (String camp : keys) {
				List<?> data = cfg.getList("positions." + camp);
				camps.add(new Camp(
						camp,
						null,
						new double[] {(double) data.get(0), (double) data.get(1), (double) data.get(2)},
						(boolean) data.get(3)
						));
			}
		
		size = cfg.getDouble("settings.camp_size");
		
		ratio = cfg.getDouble("settings.ratio");
	}
	
	public static void assignRandomTeam() {
		List<Team> teams = TeamMngr.get();
		for (Camp camp : camps) {
			int i = (int) (Math.random() * teams.size());
			camp.setOwner(teams.get(i));
			teams.remove(i);
		}
	}
	
	// end case
	public static void clearCamps() {
		for (Camp camp : camps) {
			camp.setOwner(null);
		}
	}
	
	/*
	 * ============================================================================
	 *                                    FLAGS
	 * ============================================================================
	 */
	
	public static void initFlags() {
		assignRandomTeam();
		for (Camp c : camps)
			updateFlag(c);
	}
	
	private static void updateFlag(Camp c) {
		Material mat = null; 
		if (c.getRivals().size() > 1)
			mat = Material.GRAY_CONCRETE;
		else
			mat = TeamMngr.getTeamMaterial(c.getOwner());
		placeFlag(c, mat);
	}
	
	private static void placeFlag(Camp camp, Material mat) {
		Location loc = camp.getLoc().add(0, 11.5, 0);
		double trig = (camp.getDir() ? 1 : 0) * (Math.PI/2);        // transform a boolean into 0 or 1 and *pi/2 to get a direction
		double[] dir = new double[] {Math.cos(trig), Math.sin(trig)};
		int i = 0;
		while (i<10) {
			loc.add(0, -1, 0);
			if (loc.getBlock().getType() != Material.OAK_FENCE) loc.getBlock().setType(Material.OAK_FENCE);
			if (i<3) {
				loc.add(dir[0], 0, dir[1]).getBlock().setType(mat);
				loc.add(-2*dir[0], 0, -2*dir[1]).getBlock().setType(mat);
				loc.add(dir[0], 0, dir[1]);
			}
			i++;
		}
	}
	
	public static void clearFlags() {
		for (Camp c : camps)
			placeFlag(c, Material.WHITE_CONCRETE);
	}
	
	/*
	 * ============================================================================
	 *                              UTIL FUNCTIONS
	 * ============================================================================
	 */
	
	public static void conquest(Camp c, Team t) {
		Conquest.addConquest(c, t);
		placeFlag(c, Material.GRAY_CONCRETE);
	}
	
	public static void capture(Camp c, Team t) {
		if (c.getOwner() == t) {
			for (Player p : TeamMngr.getPlayers(t))
				p.playSound(c.getLoc(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
		} else {
			if (c.getRivals().contains(t)) {
				conquestMessage(c.getOwner(), t);
			} else
				stealMessage(c.getOwner(), t);
			Main.map.playSound(c.getLoc(), Sound.ENTITY_ENDER_DRAGON_GROWL, SoundCategory.MASTER, 400.0f, 1.2f);
			for (Player p : TeamMngr.getPlayers(t))
				p.playSound(c.getLoc(), Sound.BLOCK_BEACON_ACTIVATE, SoundCategory.MASTER, 10.0f, 1.0f);
		}
		c.setOwner(t);
		placeFlag(c, TeamMngr.getTeamMaterial(t));
		EndGameMngr.winCondition(t);
	}
	
	private static void conquestMessage(Team from, Team to) {
		String msgFrom = Func.format("&eLes " + to.getDisplayName() + "&e ont conquis votre camp!");
		String msgTo = Func.format("&eVotre équipe a conquis le camp des " + from.getDisplayName());
		String msgDefault = Func.format("&eLe camp des " + from.getDisplayName() + "&e a été conquis par les " + to.getDisplayName());
		for (Player p : TeamMngr.getTeamedPlayers()) {
			Team pteam = TeamMngr.teamOf(p);
			if (pteam == from) p.sendMessage(msgFrom);
			else if (pteam == to) p.sendMessage(msgTo);
			else p.sendMessage(msgDefault);
		}
	}
	
	private static void stealMessage(Team from, Team to) {
		String msgFrom = Func.format("&eLes " + to.getDisplayName() + "&e ont volés votre camp!");
		String msgTo = Func.format("&eVotre équipe a volé le camp des " + from.getDisplayName());
		String msgDefault = Func.format("&eLe camp des " + from.getDisplayName() + "&e a été volé par les " + to.getDisplayName());
		for (Player p : TeamMngr.getTeamedPlayers()) {
			Team pteam = TeamMngr.teamOf(p);
			if (pteam == from) p.sendMessage(msgFrom);
			else if (pteam == to) p.sendMessage(msgTo);
			else p.sendMessage(msgDefault);
		}
	}

	public static List<Camp> getTeamCamps(Team t) {
		List<Camp> res = new ArrayList<>();
		for (Camp camp : camps)
			if (camp.isOwner(t))
				res.add(camp);
		return res;
	}
	
	public static boolean teamProtect(Team t, Camp c) {
		for (Player p : TeamMngr.getAliveTeamMembers(t))
			if (isInVitalSpace(p.getLocation(), c))
				return true;
		return false;
	}
	
	public static boolean isInCamp(Location loc, Camp camp) {
		double[] pos = camp.getPos();
		return isNearBy(loc.getX(), pos[0], size) && isNearBy(loc.getZ(), pos[2], size);
	}
	
	public static boolean isInVitalSpace(Location loc, Camp camp) {
		double[] pos = camp.getPos();
		double vsSize = ratio * size;
		return isNearBy(loc.getX(), pos[0], vsSize) && isNearBy(loc.getZ(), pos[2], vsSize);
	}
	
	public static boolean isFlag(Location loc) {
		boolean isFlag = false;
		double x = loc.getX(), y = loc.getY(), z = loc.getZ();
		for (Camp c : camps) {
			double[] cp = c.getPos();
			if (isNearBy(x, cp[0], 0.5) && isNearBy(y, cp[1] + 6, 5.5) && isNearBy(z, cp[2], 0.5)) {
				isFlag = true;
			}
			if (isNearBy(y, cp[1] + 9.5, 1.5)) {
				if (c.getDir()) {
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
	
	private static boolean isNearBy(double pos, double around, double by) {
		return around - by <= pos && pos < around + by;
	}
	
}
