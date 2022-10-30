package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.util.Func;

public class MagmaChamber extends BukkitRunnable {
	
	private static final int FORTY_FIVE_SECS = 45;
	private static final int ONE_MIN = 1 * 60;
	private static final int SEVEN_MINS = 7 * 60;
	private static final int EIGHT_MINS = 8 * 60;
	
	private Game game;
	private double height;
	
	private Set<Player> in = new HashSet<>();
	private boolean froze;
	private int chrono = 0;
	
	private List<Location> lava = new ArrayList<>();
	private List<Location> lavaCache;
	
	public MagmaChamber(Game game, double height) {
		this.game = game;
		this.height = height;
		MapMngr.mapBlocks(game.getWorld(), -63, height, e -> {
			Location loc = new Location(game.getWorld(), e[0], e[1], e[2]);
			if (loc.getBlock().getType() == Material.LAVA)
				lava.add(loc);
		});
		resetCache();
	}
	
	@Override
	public void run() {
		if (!game.pvp()) return;
		
		int r = chrono++ % EIGHT_MINS;
		
		if (froze) {
			if (r == FORTY_FIVE_SECS)
				game.broadcastPlayers(Func.format(Main.MSG_WARNING + "La chambre magmatique commence à se rechauffer"));
			else if (r > FORTY_FIVE_SECS)
				unfreezeRandomLava((int) (0.07*lava.size()));
			else if (r >= ONE_MIN)
				unfreezeChamber();
		} else {
			updatePlayersIn();
			if (r == SEVEN_MINS)
				game.broadcastPlayers(Func.format(Main.MSG_ANNOUNCE + "La chambre magmatique commence à se refroidir"));
			else if (r > SEVEN_MINS)
				freezeRandomLava((int) (0.015*lava.size()));
			else if (r == 0)
				freezeChamber();
		}
		
	}
	
	private void updatePlayersIn() {
		for (Player p : in) {
			if (!isIn(p.getLocation()))
				in.remove(p);
			if (p.getGameMode() == GameMode.SURVIVAL && p.getFireTicks() <= 0)
				p.setFireTicks(40);
		}
	}
	
	public boolean isIn(Location loc) {
		return loc.getY()<height;
	}
	
	public boolean isFrozen() {
		return froze;
	}
	
	public void add(Player p) {
		in.add(p);
	}
	
	private void freezeChamber() {
		if (froze) return;
		game.broadcastPlayers(Func.format(Main.MSG_ANNOUNCE + "La chambre magmatique s'est refroidie!"));
		for (Location loc : lava) {
			Block b = loc.getBlock();
			if (b.getType() == Material.LAVA)
				b.setType(Material.OBSIDIAN);
		}
		froze = true;
	}
	
	private void unfreezeChamber() {
		if (!froze) return;
		game.broadcastPlayers(Func.format(Main.MSG_WARNING + "La chambre magmatique s'est réchauffee!"));
		for (Location loc : lava) {
			Block b = loc.getBlock();
			if (b.getType() == Material.OBSIDIAN)
				b.setType(Material.LAVA);
		}
		froze = false;
	}
	
	private void freezeRandomLava(int count) {
		changeRandomLava(Material.OBSIDIAN, count);
	}
	
	private void unfreezeRandomLava(int count) {
		changeRandomLava(Material.LAVA, count);
	}
	
	private void changeRandomLava(Material mat, int count) {
		for (int i = 0; i < count; i++) {
			Location randomLava = pickLava();
			if (randomLava != null)
				randomLava.getBlock().setType(mat);
		}
	}
	
	private Location pickLava() {
		int size = lavaCache.size();
		if (size == 0) return null;
		int ir = Func.randomInt(0, size);
		Location res = lavaCache.get(ir);
		lavaCache.remove(ir);
		return res;
	}
	
	private void resetCache() {
		lavaCache = new ArrayList<>(lava);
	}
	
}