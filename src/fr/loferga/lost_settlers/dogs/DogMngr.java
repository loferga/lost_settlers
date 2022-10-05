package fr.loferga.lost_settlers.dogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.Func;

public class DogMngr {
	
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
	 *                                 ORDERS
	 * ============================================================================
	 */

	private static Map<Player, LivingEntity> prevTarget = new HashMap<>();
	
	public static void onOrder(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (DogMngr.get().containsKey(p) && !e.hasItem()) {
			LivingEntity target = getTarget(p);
			if (target != null) {
				List<Wolf> dogs = DogMngr.get().get(p);
				if (target instanceof Wolf && dogs.contains(target)) {
					if (!ComeBack.containPlayer(p)) {
						comeBack(dogs, p);
						p.getWorld().playSound(p.getLocation(), "custom.whistle_back", SoundCategory.PLAYERS, 2.0f, 1.0f);
						for (Wolf dog : dogs)
							Func.glowFor((LivingEntity)dog, new HashSet<>(Set.of(p)), 10);
					}
					prevTarget.remove(p);
				} else if (!(target instanceof Player) || GameMngr.gameIn(p).pvp()) {
					setAnger(dogs, target);
					p.getWorld().playSound(p.getLocation(), "custom.whistle", SoundCategory.PLAYERS, 2.0f, 1.0f);
					Func.glowFor(target, new HashSet<>(Set.of(p)), 10);
					prevTarget.put(p, target);
				}
			}
		}
	}
	
	private static LivingEntity getTarget(Player p) {
		LivingEntity target = null;
		Location rayloc = p.getEyeLocation();
		Vector eyedir = rayloc.getDirection().normalize();
		boolean stop = false;
		int i = 0;
		Collection<Entity> ents = p.getWorld().getEntitiesByClasses(LivingEntity.class);
    	ents.remove(prevTarget.get(p));
    	ents.removeAll(TeamMngr.teamOf(p).getPlayers());
    	while (i < 60 && !stop) {
    		rayloc = rayloc.add(eyedir);
    		if (!rayloc.getBlock().getType().isOccluding()) {
    			for (Entity ent : ents)
    				if (ent != prevTarget.get(p) && rayloc.distance(ent.getLocation().add(0, ent.getHeight()/2, 0)) < 1.0) {
    					target = (LivingEntity) ent;
    					stop = true;
    				}
    		} else
    			stop = true;
    		i++;
    	}
    	return target;
    }
	
	private static void setAnger(List<Wolf> wolfs, LivingEntity target) {
		for (Wolf wolf : wolfs) {
			ComeBack.removeDog(wolf);
			Anger.addAnger(wolf, target);
		}
	}
	
	public static void comeBack(List<Wolf> wolfs, Player p) {
		LivingEntity dummy = (LivingEntity) p.getWorld().spawnEntity(new Location(p.getWorld(), 0, 0, 0), EntityType.BAT);
		dummy.setInvisible(true);
		dummy.setInvulnerable(true);
		dummy.setGravity(false);
		dummy.setAI(false);
		dummy.setSilent(true);
		for (Wolf wolf : wolfs) {
			Anger.removeAnger(wolf);
			ComeBack.addDog(wolf, dummy, p);
		}
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
	
	public static void transferDogsTo(Player from, Player to) {
		if (tamers.containsKey(from)) {
			for (Wolf wolf : tamers.get(from)) {
				dogGive(to, wolf);
				wolf.setAngry(false);
				wolf.getWorld().playSound(wolf.getLocation(), Sound.ENTITY_WOLF_WHINE, 1.0f, 1.0f);
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
		ChatColor color = TeamMngr.teamOf(from).getChatColor();
		from.sendMessage(Func.format("&eVous venez de confier " + color + wolf.getCustomName() +
				"&e à " + color + to.getName()));
		to.sendMessage(Func.format(color + from.getName() + "&e viens de vous confier " + color + wolf.getCustomName()));
	}
	
	private static void dogGive(Player p, Wolf w) {
		addWolf(p, w);
		w.setCollarColor(TeamMngr.teamOf(p).getDyeColor());
		w.setSitting(false);
	}

}
