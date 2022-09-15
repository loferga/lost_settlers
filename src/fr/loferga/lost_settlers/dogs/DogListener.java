package fr.loferga.lost_settlers.dogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.gui.GUIMngr;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class DogListener implements Listener {
	
	// Add Wolf
	@EventHandler
	public void onEntityTamed(EntityTameEvent e) {
		if (e.getEntity() instanceof Wolf) {
			AnimalTamer p = e.getOwner();
			Wolf wolf = (Wolf) e.getEntity();
			DogMngr.addWolf(p, wolf);
			wolf.setCollarColor(TeamMngr.teamOf((Player) p).getDyeColor());
			wolf.setCustomName(pickRandomName());
		}
	}
	
	private static List<String> names = new ArrayList<>(Arrays.asList(
			"Kylian", "Nutella", "Rexma", "Scooby", "Jean-Luc Mélenchon", "Alexis Corbière", "Bellatar", "Douglas", "Squeezie",
			"Fluffy", "Marex"
			));
	
	private static String pickRandomName() {
		int rng = (int) ThreadLocalRandom.current().nextInt(names.size());
		String picked = names.get(rng);
		names.remove(rng);
		return picked;
	}
	
	// Avoid Wolf teleport while they are in Order Task
	@EventHandler
	public void onEntityTeleport(EntityTeleportEvent e) {
		if (e.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf)e.getEntity();
			if (Anger.contain(wolf) || ComeBack.contain(wolf))
				e.setCancelled(true);
		}
	}
	
	// Wolf Remove
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) e.getEntity();
			if (wolf.isTamed()) DogMngr.removeWolf((Player) wolf.getOwner(), wolf);
			Anger.removeAnger(wolf);
			ComeBack.removeDog(wolf);
		}
		Set<Wolf> angry = Anger.getDogAngryAt(e.getEntity());
		if (!angry.isEmpty())
			for (Wolf w : angry) {
				Player ow = (Player) w.getOwner();
				Anger.removeAnger(w);
				DogMngr.comeBack(List.of(w), ow);
			}
	}
	
	// Dog Transfer
	@EventHandler
	public void onPlayerInteractWithDog(PlayerInteractAtEntityEvent e) {
		Player p = e.getPlayer();
		Game game = GameMngr.gameIn(p);
		if (game != null && p.isSneaking())
			if (e.getRightClicked() instanceof Wolf)
				if (DogMngr.ownership((Wolf) e.getRightClicked(), p))
					if (game.getMembers(TeamMngr.teamOf(p)).size() > 1)
						p.openInventory(GUIMngr.getDTM(p, e.getRightClicked().getCustomName()));
	}

}