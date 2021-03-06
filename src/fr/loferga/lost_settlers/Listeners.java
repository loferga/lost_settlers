package fr.loferga.lost_settlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.GlowSquid;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import fr.loferga.lost_settlers.dogs.ComeBack;
import fr.loferga.lost_settlers.dogs.Anger;
import fr.loferga.lost_settlers.dogs.DogsMngr;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.gui.GUIMngr;
import fr.loferga.lost_settlers.map.CloseWorld;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.rules.Wounded;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Listeners implements Listener {
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		CloseWorld.reconnect(e.getPlayer());
	}
	
	// events with multiples effects
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		// linked to CAMPS & JUMP
		Player p = e.getPlayer();
		Location from = e.getFrom();
		Location to = e.getTo();
		Game g = GameMngr.gameIn(p);
		if (g != null) {
			if (g.pvp() && p.getGameMode() == GameMode.SURVIVAL)
				if ((int)from.getX() != (int)to.getX() || (int)from.getZ() != (int)to.getZ())
					campArea(p);
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		// linked to NATURAL REGEN & NO DAMAGE BONUS ARROW
		if (e.getEntity() instanceof Player) {
			Player dmged = (Player) e.getEntity();
			if (e.getDamager() instanceof Player) {
				Player dmger = (Player) e.getDamager();
				Game game = GameMngr.gameIn(dmger);
				if (game != null && (game.pvp() || TeamMngr.teamOf(dmged) == TeamMngr.teamOf(dmger))) {
					putInCombat(dmged, dmger);
				} else
					e.setCancelled(true);
				
			} else if (e.getDamager() instanceof Arrow) {
				if (isBonusArrow((Arrow) e.getDamager())) e.setDamage(0);
			} else if (e.getDamager() instanceof SpectralArrow) {
				SpectralArrow sa = (SpectralArrow) e.getDamager();
				if (sa.getShooter() instanceof Player) {
					Func.glowFor(dmged, TeamMngr.teamOf((Player) sa.getShooter()).getPlayers(), 600);
				}
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (GameMngr.getGame(e.getBlock().getWorld()) != null)
			e.setCancelled(
					campBlockInteract(e.getBlock(), e.getPlayer())
					);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		// linked to CAMPS & ORE EXP
		if (GameMngr.gameIn(e.getPlayer()) != null) {
			if (campBlockBreak(e.getBlock(), e.getPlayer())) {
				e.setCancelled(true);
				return;
			}
			oreExp(e.getBlock());
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (GameMngr.gameIn(e.getPlayer()) != null) {
			if (e.getAction() == Action.LEFT_CLICK_AIR) {
				onOrder(e);
			} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				onWaterPlaced(e);
				e.setCancelled(campBlockBreak(e.getClickedBlock(), e.getPlayer()));
			}
		}
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		Entity ent = e.getEntity();
		if (ent instanceof GlowSquid)
			e.setCancelled(true);
		else if (ent instanceof Monster)
			if (ent.getLocation().getY() < 16)
				spawnMagmaCubeInstead(ent);
	}
	
	/*
	 * ============================================================================
	 *                                  CAMP
	 * ============================================================================
	 */
	
	private static boolean campBlockBreak(Block b, Player p) {
		Game game = GameMngr.gameIn(p);
		if (game.isFlag(b.getLocation().add(0.5, 0.5, 0.5)))
			return true;
		return campBlockInteract(b, p);
	}
	
	private static boolean campBlockInteract(Block b, Player p) {
		if (b.getType() != Material.TNT) {
			Game game = GameMngr.gameIn(p);
			LSTeam pteam = TeamMngr.teamOf(p);
			if (game != null && pteam != null) {
				Camp camp = game.campIn(b.getLocation());
				if (camp != null && !camp.getRivals().contains(pteam)) {
					Func.sendActionbar(p, Func.format("&cCe camp n'est pas ? vous"));
					return true;
				}
			}
		}
		return false;
	}
	
	private static void campArea(Player p) {
		// triggered whenever a player move to a new block in x.z plane
		if (!p.isInvisible()) {
			Game game = GameMngr.gameIn(p);
			LSTeam pteam = TeamMngr.teamOf(p);
			if (game != null && pteam != null) {
				Camp camp = game.vitalIn(p.getLocation());
				if (camp != null && !camp.getRivals().contains(pteam)) {
					if (game.teamProtect(camp.getOwner(), camp))
						game.conquest(camp, pteam);
					else
						game.capture(camp, pteam);
				}
			}
		}
	}
	
	/*
	 * ============================================================================
	 *                                    WOLFS
	 * ============================================================================
	 */
	
	
	// Add Wolf
	@EventHandler
	public void onEntityTamed(EntityTameEvent e) {
		if (e.getEntity() instanceof Wolf) {
			AnimalTamer p = e.getOwner();
			Wolf wolf = (Wolf) e.getEntity();
			DogsMngr.addWolf(p, wolf);
			wolf.setCollarColor(TeamMngr.teamOf((Player) p).getDyeColor());
			wolf.setCustomName(pickRandomName());
		}
	}
	
	private static List<String> names = new ArrayList<>(Arrays.asList(
			"Kylian", "Nutella", "Rexma", "Scooby", "Jean-Luc M?lenchon", "Alexis Corbi?re", "Bellatar", "Douglas", "Squeezie",
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
	
	/*
	 * Wolf Control
	 */
	
	private static Map<Player, LivingEntity> prevTarget = new HashMap<>();
	
	private static void onOrder(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		if (DogsMngr.get().containsKey(p) && !e.hasItem()) {
			LivingEntity target = getTarget(p);
			if (target != null) {
				List<Wolf> dogs = DogsMngr.get().get(p);
				if (target instanceof Wolf && dogs.contains(target)) {
					if (!ComeBack.containPlayer(p)) {
						comeBack(dogs, p);
						p.getWorld().playSound(p.getLocation(), "custom.whistle_back", SoundCategory.PLAYERS, 2.0f, 1.0f);
						for (Wolf dog : dogs)
							Func.glowFor((LivingEntity)dog, new HashSet<>(Set.of(p)), 10);
					}
					prevTarget.remove(p);
				} else {
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
    		++i;
    	}
    	return target;
    }
	
	private static void setAnger(List<Wolf> wolfs, LivingEntity target) {
		for (Wolf wolf : wolfs) {
			ComeBack.removeDog(wolf);
			Anger.addAnger(wolf, target);
		}
	}
	
	private static void comeBack(List<Wolf> wolfs, Player p) {
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
	
	// Wolf Remove
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) e.getEntity();
			if (wolf.isTamed()) DogsMngr.removeWolf((Player) wolf.getOwner(), wolf);
			Anger.removeAnger(wolf);
			ComeBack.removeDog(wolf);
		}
		Wolf angry = Anger.getDogAngry(e.getEntity());
		if (angry != null) {
			Player p2 = (Player) angry.getOwner();
			comeBack(DogsMngr.get().get(p2), p2);
		}
	}
	
	/*
	 * ============================================================================
	 *                                     GUI
	 * ============================================================================
	 */
	
	@EventHandler
	public void onPlayerInteractWithDog(PlayerInteractAtEntityEvent e) {
		Player p = e.getPlayer();
		Game game = GameMngr.gameIn(p);
		if (game != null && p.isSneaking())
			if (e.getRightClicked() instanceof Wolf)
				if (DogsMngr.ownership((Wolf) e.getRightClicked(), p))
					if (game.getMembers(TeamMngr.teamOf(p)).size() > 1)
						p.openInventory(GUIMngr.getDTM(p, e.getRightClicked().getCustomName()));
	}
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		if (e.getPlayer().getWorld() == Main.hub &&
				e.getPlayer().getGameMode() == GameMode.ADVENTURE &&
				!e.getPlayer().getScoreboardTags().contains("noSelection")
				)
			e.getPlayer().openInventory(GUIMngr.getTM());
	}
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (GameMngr.gameIn((Player) e.getView().getPlayer()) == null) {
			if (e.getView().getTitle() == "Selection") {
				if (e.getCurrentItem() != null)
					GUIMngr.clickTM((Player) e.getWhoClicked(), e.getCurrentItem());
				e.setCancelled(true);
			}
		} else if (e.getView().getTitle().startsWith("Confier")) {
			if (e.getCurrentItem() != null)
				GUIMngr.clickDTM((Player) e.getWhoClicked(), e.getCurrentItem());
			e.setCancelled(true);
		}
	}
	
	/*
	 * ============================================================================
	 *                                     RULES
	 * ============================================================================
	 */
	
	// ##### ORES XP #####
	
	private void oreExp(Block b) {
		if (isOre(b.getType().toString()))
			spawnExp(b);
	}
	
	private static boolean isOre(String bTypeN) {
		return bTypeN.length() > 7 && (bTypeN.endsWith("ORE") || bTypeN.equals("ANCIENT_DEBRIS"));
	}
	
	private static void spawnExp(Block b) {
		ExperienceOrb exp = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation(), EntityType.EXPERIENCE_ORB);
		if (b.getType() != Material.ANCIENT_DEBRIS)
			exp.setExperience(oreToExp(b.getType().toString()));
		else {
			exp.setExperience(10);
			for (int i = 0; i<6; i++) {
				ExperienceOrb netheriteXPOrb = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation(), EntityType.EXPERIENCE_ORB);
				netheriteXPOrb.setExperience(10);
			}
		}
	}

	private static int oreToExp(String name) {
		int fact = 1;
		if (name.startsWith("DEEPSLATE")) {
			name = name.substring(10, name.length() - 4);
			fact = 2;
		} else
			name = name.substring(0, name.length() - 4);
		switch (name) {
		case "COAL": return 3 * fact;
		case "IRON": return 4 * fact;
		case "COPPER": return 5 * fact;
		case "GOLD": return 6 * fact;
		case "REDSTONE": return 8 * fact;
		case "LAPIS": return 12 * fact;
		case "DIAMOND": return 14 * fact;
		case "EMERALD": return 20 * fact;
		default: return 0;
		}
	}
	
	// ##### MAGMA CHAMBER #####
	
	@EventHandler
	public void onObsidianForm(BlockFormEvent e) {
		if (e.getNewState().getType() == Material.OBSIDIAN) {
			Location bloc = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
			bloc.getWorld().playSound(bloc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
			bloc.getWorld().spawnParticle(Particle.SMOKE_LARGE, bloc, 8, 0.4, 0.4, 0.4, 0.01);
			e.getBlock().setType(Material.STONE);
			e.setCancelled(true);
		}
	}
	
	private void onWaterPlaced(PlayerInteractEvent e) {
		if (e.hasItem())
			if (e.getItem().getType() == Material.WATER_BUCKET)
				if (e.getClickedBlock() != null) {
					Location wloc = e.getClickedBlock().getLocation().add(
							e.getBlockFace().getDirection().add(new Vector(0.5, 0.5, 0.5))
							);
					if (wloc.getY() < 16) {
						Player p = e.getPlayer();
						p.playSound(wloc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
						p.spawnParticle(Particle.SMOKE_LARGE, wloc, 8, 0.4, 0.4, 0.4, 0.01);
						e.setCancelled(true);
					}
				}
	}

	@EventHandler
	public void vaporizeWater(BlockPhysicsEvent e) {
		Block b = e.getBlock();
		if (b.getType() == Material.WATER)
			if (MapMngr.worlds.contains(b.getWorld()) && b.getLocation().getY() < 16)
				b.setType(Material.AIR);
	}
	
	private static void spawnMagmaCubeInstead(Entity ent) {
		MagmaCube magma = (MagmaCube) ent.getWorld().spawnEntity(ent.getLocation(), EntityType.MAGMA_CUBE);
		magma.setSize((int) (Math.random() * 4));
		ent.remove();
	}
	
	// ##### NO DAMAGE BONUS ARROW #####
	
	private static PotionEffectType[] badEffects = new PotionEffectType[] {
			PotionEffectType.HARM,
			PotionEffectType.POISON,
			PotionEffectType.WEAKNESS,
			PotionEffectType.SLOW
		};
	
	private static boolean isBonusArrow(Arrow a) {
		boolean bonus = a.getCustomEffects().size() > 0;
		int i = 0, length = a.getCustomEffects().size();
		while (i<length && bonus) {
			if (Func.primeContain(badEffects, a.getCustomEffects().get(i)))
				bonus = false;
			i++;
		}
		return bonus;
	}
	
	// ##### NATURAL REGEN #####
	
	private static void putInCombat(Player dmged, Player dmger) {
		Wounded.addPlayer(dmged);
		Wounded.addPlayer(dmger);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (GameMngr.gameIn((Player) e.getEntity()) != null)
				if (e.getCause() != DamageCause.FIRE_TICK)
					Wounded.addPlayer((Player) e.getEntity());
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (e.getEntity() instanceof Player)
			if (Wounded.isInCombat((Player) e.getEntity()))
				e.setCancelled(true);
	}
	
	// ##### DEATH #####
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (GameMngr.gameIn(e.getPlayer()) != null) {
			Player p = e.getPlayer();
			p.setGameMode(GameMode.SPECTATOR);
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Game game = GameMngr.gameIn(e.getEntity());
		if (game != null) {
			Player dead = e.getEntity();
			dead.setBedSpawnLocation(dead.getLocation());
			if (game.pvp()) {
				Player killer = dead.getKiller();
				if (killer != null) {
					if (killer instanceof Player) {
						game.kill(dead, killer);
						DogsMngr.transferDogsTo(dead, killer);
					}
				}
				game.winCondition(null);
				e.setDroppedExp(dead.getTotalExperience());
			} else
				game.addRespawn(dead);
			GUIMngr.refreshDTM();
		}
	}
	
	// ##### CRAFTS COLOR ASSIGNMENT #####
	
	@EventHandler
	public void onPlayerCraftItem(PrepareItemCraftEvent e) {
		// FIREWORK
		ItemStack result = e.getInventory().getResult();
		if (result != null) {
			if (result.getType() == Material.FIREWORK_ROCKET) {
				Player p = (Player) e.getView().getPlayer();
				Color color = TeamMngr.teamOf(p).getColor();
				result.setItemMeta((ItemMeta) Recipes.setColor(result.getItemMeta(), color));
				e.getInventory().setResult(result);
			}
		}
	}
	
	// ##### NETHERITE SMITHING #####
	
	@EventHandler
	public void onPlayerSmithItem(PrepareSmithingEvent e) {
		ItemStack compound = e.getInventory().getItem(1);
		if (compound != null && compound.getType() == Material.NETHERITE_INGOT) {
			if (e.getInventory().getItem(0) != null && e.getResult() != null) {
				ItemMeta im = e.getResult().getItemMeta();
				Map<Enchantment, Integer> enchantments = im.getEnchants();
				for (Enchantment ench : enchantments.keySet())
					im.addEnchant(ench, enchantments.get(ench) + 1, true);
				e.getResult().setItemMeta(im);
			}
		}
	}
	
	// ##### ENDER PEARL DAMAGE MODIFIER #####
	
	@EventHandler
	public void onPlayerTeleport(PlayerTeleportEvent e) {
		if (GameMngr.gameIn(e.getPlayer()) != null && e.getCause() == TeleportCause.ENDER_PEARL) {
			Player p = (Player) e.getPlayer();
			p.setHealth(2*p.getHealth()/3);
		}
	}
	
	// ##### INVISIBLE POTION PARTICLES #####
	
	@EventHandler
	public void onPotionEffect(EntityPotionEffectEvent e) {
		if (e.getEntity() instanceof Player) {
			Player p = (Player) e.getEntity();
			PotionEffect eff = e.getNewEffect();
			if (e.getNewEffect() != null && !e.getNewEffect().isAmbient()) {
				String effN = e.getNewEffect().getType().getName();
				if (effN.equals("INVISIBILITY")) {
					p.addPotionEffect(new PotionEffect(eff.getType(), eff.getDuration(), eff.getAmplifier(), true, false));
					e.setCancelled(true);
				} else if (effN.equals("FIRE_RESISTANCE")) {
					p.addPotionEffect(new PotionEffect(eff.getType(), (int) (80 + eff.getDuration()/25), eff.getAmplifier(), true, true));
					e.setCancelled(true);
				} else if (effN.equals("INCREASE_DAMAGE")) {
					p.addPotionEffect(new PotionEffect(eff.getType(), (int) (60 + eff.getDuration()/20), eff.getAmplifier(), true, true));
					e.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		CloseWorld.disconect(e.getPlayer());
		Wolf angry = Anger.getDogAngry((LivingEntity) e.getPlayer());
		if (angry != null) {
			Player owner = (Player) angry.getOwner();
			comeBack(DogsMngr.get().get(owner), owner);
		}
	}

}