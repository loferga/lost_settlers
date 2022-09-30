package fr.loferga.lost_settlers.map.camps;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;

import fr.loferga.lost_settlers.teams.LSTeam;

public class Camp {

	public Camp(String name, LSTeam team, Location location, boolean direction, double vSize) {
		this.name = name;
		this.owners = new ArrayList<>(Arrays.asList(new Owner(team, System.currentTimeMillis())));
		this.rivals = new ArrayList<>(Arrays.asList(team));
		this.location = location;
		this.direction = direction;
		this.zoneEffect = new ZoneEffect(this, vSize);
	}

	private String name;
	private List<Owner> owners;
	private List<LSTeam> rivals;
	private Location location;
	private boolean direction;

	private ZoneEffect zoneEffect;
	
	public void killZoneEffect() {
		zoneEffect.stop();
	}

	public String getName() {
		return name;
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

	public boolean getDirection() {
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

	public void placeFlag(Material mat) {
		Location loc = location.clone().add(0, 11.0, 0);
		double trig = (direction ? 1 : 0) * (Math.PI / 2); // transform a boolean into 0 or 1 and *pi/2 to get a
															// direction
		double[] dir = new double[] { Math.cos(trig), Math.sin(trig) };
		int i = 0;
		while (i < 11) {
			loc.add(0, -1, 0);
			if (loc.getBlock().getType() != Material.OAK_FENCE)
				loc.getBlock().setType(Material.OAK_FENCE);
			if (i < 3) {
				loc.add(dir[0], 0, dir[1]).getBlock().setType(mat);
				loc.add(-2 * dir[0], 0, -2 * dir[1]).getBlock().setType(mat);
				loc.add(dir[0], 0, dir[1]);
			}
			i++;
		}
		loc.add(dir[0], 0, dir[1]).getBlock().setType(Material.ENCHANTING_TABLE);
		loc.add(-2 * dir[0], 0, -2 * dir[1]).getBlock().setType(Material.CHISELED_STONE_BRICKS);
		loc.add(0, 1, 0).getBlock().setType(Material.BREWING_STAND);
	}

}
