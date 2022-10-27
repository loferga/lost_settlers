package fr.loferga.lost_settlers.map.camps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import fr.loferga.lost_settlers.map.geometry.Vector;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Camp {

	public Camp(String name, Location location, Direction direction) {
		this.name = name;
		this.owners = new ArrayList<>(Arrays.asList(new Owner(TeamMngr.NULL, System.currentTimeMillis())));
		this.rivals = new ArrayList<>(Arrays.asList(TeamMngr.NULL));
		this.location = location;
		this.direction = direction;
	}

	public String name;
	private List<Owner> owners;
	private List<LSTeam> rivals;
	private Location location;
	private Direction direction;
	private Set<Block> flag = new HashSet<>();

	private ZoneEffect zoneEffect;
	
	public void startZoneEffect() {
		zoneEffect = new ZoneEffect(this);
	}
	
	public void killZoneEffect() {
		zoneEffect.stop();
	}

	public LSTeam getOwner() {
		return owners.get(owners.size() - 1).getTeam();
	}

	public List<LSTeam> getRivals() {
		return rivals;
	}

	public double[] getPosition() {
		return new double[] { location.getX(), location.getY(), location.getZ() };
	}

	public Direction getDirection() {
		return direction;
	}

	public boolean isOwner(LSTeam team) {
		return getOwner() == team;
	}

	public void setOwner(LSTeam team) {
		owners.add(new Owner(team, System.currentTimeMillis()));
		rivals = new ArrayList<>(Arrays.asList(team));
	}

	public void addRival(LSTeam team) {
		rivals.add(team);
	}

	public void removeRival(LSTeam team) {
		rivals.remove(team);
	}

	public Location getLocation() {
		return location.clone();
	}

	public long getOwnerTime() {
		return owners.get(owners.size() - 1).getTime();
	}

	public void placeFlag() {
		Location loc = location.clone().add(0, 11.0, 0);
		Vector dir = direction.vector;
		Vector minus2dir = dir.clone().multiply(-2);
		Material flagMat = getOwner().getFlag();
		Block b;
		int i = 0;
		while (i < 11) {
			loc.add(0, -1, 0);
			b = loc.getBlock();
			flag.add(b);
			if (b.getType() != Material.OAK_FENCE)
				b.setType(Material.OAK_FENCE);
			if (i < 3) {
				b = dir.addTo(loc).getBlock(); flag.add(b); b.setType(flagMat);
				b = minus2dir.addTo(loc).getBlock(); flag.add(b); b.setType(flagMat);
				dir.addTo(loc);
			}
			i++;
		}
		b = dir.addTo(loc).getBlock(); flag.add(b); b.setType(Material.ENCHANTING_TABLE);
		b = minus2dir.addTo(loc).getBlock(); flag.add(b); b.setType(Material.CHISELED_STONE_BRICKS);
		b = loc.add(0, 1, 0).getBlock(); flag.add(b);b.setType(Material.BREWING_STAND);
	}
	
	public void modifyFlag(Material mat) {
		Location loc = location.clone().add(0, 10, 0);
		Vector dir = direction.vector;
		Vector minus2dir = dir.clone().multiply(-2);
		Vector downdir = dir.clone(); downdir.y = -1;
		for (int i = 0; i<3; i++) {
			dir.addTo(loc).getBlock().setType(mat);
			minus2dir.addTo(loc).getBlock().setType(mat);
			downdir.addTo(loc);
		}
	}
	
	public boolean isFlag(Block b) {
		return flag.contains(b);
	}
	
	@Override
	public String toString() {
		return name;
	}

}
