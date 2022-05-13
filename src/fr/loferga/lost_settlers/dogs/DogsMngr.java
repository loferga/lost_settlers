package fr.loferga.lost_settlers.dogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Sound;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class DogsMngr {
	
	protected static Map<AnimalTamer, List<Wolf>> tamers = new HashMap<>();
	
	public static Map<AnimalTamer, List<Wolf>> get() {
		return new HashMap<>(tamers);
	}
	
	public static void addWolf(AnimalTamer p, Wolf wolf) {
		if (tamers.containsKey(p)) {
			tamers.get(p).add(wolf);
		} else tamers.put(p, new ArrayList<>(Arrays.asList(wolf)));
		wolf.setOwner(p);
	}
	
	public static void removeWolf(AnimalTamer p, Wolf dog) {
		if (tamers.containsKey(p)) {
			List<Wolf> dogs = tamers.get(p);
			if (dogs.size() > 1) {
				tamers.remove(p);
			} else dogs.remove(dog);
		}
	}
	
	public static boolean ownership(Wolf wolf, Player p) {
		if (tamers.containsKey(p))
			if (tamers.get(p).contains(wolf))
				return true;
		return false;
	}
	
	/*
	 * ============================================================================
	 *                             UTIL FUNCTIONS
	 * ============================================================================
	 */
	
	public static Wolf getDogByName(Player p, String name) {
		if (tamers.containsKey(p))
			for (Wolf w : tamers.get(p))
				if (w.getCustomName().equals(name))
					return w;
		return null;
	}
	
	public static void setDogsColor(Player p, DyeColor color) {
		if (tamers.containsKey(p))
			for (Wolf wolf : tamers.get(p))
				wolf.setCollarColor(color);
	}
	
	public static void refundDogs(Player p) {
		for (Wolf wolf : Main.map.getEntitiesByClass(Wolf.class))
			if (wolf.getOwner() == p)
				addWolf(p, wolf);
	}
	
	public static void transferDogsTo(Player from, Player to) {
		if (tamers.containsKey(from)) {
			for (Wolf wolf : tamers.get(from)) {
				dogGive(to, wolf);
				wolf.setAngry(false);
				Main.map.playSound(wolf.getLocation(), Sound.ENTITY_WOLF_WHINE, 1.0f, 1.0f);
			}
			tamers.remove(from);
			to.sendMessage(Func.format("&eVous êtes désormais le maître des chiens de " +
			TeamMngr.teamOf(from).getColor() + from.getDisplayName()));
		}
	}
	
	public static void transferDogTo(Wolf wolf, Player from, Player to) {
		dogGive(to, wolf);
		if (tamers.get(from).size()>1)
			tamers.get(from).remove(wolf);
		else
			tamers.remove(from);
		ChatColor color = TeamMngr.teamOf(from).getColor();
		from.sendMessage(Func.format("&eVous venez de confier " + color + wolf.getCustomName() +
				"&e à " + color + to.getDisplayName()));
		to.sendMessage(Func.format(color + from.getDisplayName() + "&e viens de vous confier " + color + wolf.getCustomName()));
	}
	
	private static void dogGive(Player p, Wolf w) {
		addWolf(p, w);
		w.setCollarColor(TeamMngr.getDyeColor(p));
		w.setSitting(false);
	}

}
