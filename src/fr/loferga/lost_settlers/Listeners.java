package fr.loferga.lost_settlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import fr.loferga.lost_settlers.dogs.DogsMngr;
import fr.loferga.lost_settlers.game.EndGameMngr;
import fr.loferga.lost_settlers.gui.GUIMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.map.camps.CampMngr;
import fr.loferga.lost_settlers.tasks.ComeBack;
import fr.loferga.lost_settlers.tasks.DogAnger;
import fr.loferga.lost_settlers.tasks.Game;
import fr.loferga.lost_settlers.tasks.NaturalRegen;
import fr.loferga.lost_settlers.tasks.RespawnCooldown;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class Listeners implements Listener {
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (p.getGameMode() == GameMode.ADVENTURE) {
			if (Game.active()) {
				DogsMngr.refundDogs(p);
				NaturalRegen.addPlayer(p);
			} else {
				p.teleport(new Location(Bukkit.getWorld("hub"), 0.5, 10.0, 0.5));
			}
		}
	}
	
	// events with multiples effects
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		// linked to CAMPS
		Player p = e.getPlayer();
		Location from = e.getFrom();
		Location to = e.getTo();
		if (Game.active()) {
			if (Game.pvp() && p.getGameMode() == GameMode.SURVIVAL)
				if ((int)from.getX() != (int)to.getX() || (int)from.getZ() != (int)to.getZ())
					campArea(p);
		} else if (p.getGameMode() == GameMode.ADVENTURE) {
			if ((int)from.getX() != (int)to.getX() || (int)from.getY() != (int)to.getY() || (int)from.getZ() != (int)to.getZ())
				if ((int)from.getY() != (int)to.getY()) {
					if (to.getY() < 10) {
						p.setHealth(0);
					}
				} else if (to.getBlock().getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK) {
					if (p.getBedSpawnLocation() == null || p.getBedSpawnLocation().getY() < to.getY()) {
						Location abs = to.getBlock().getLocation().add(0.5, 0, 0.5);
						abs.setPitch(to.getPitch());
						abs.setYaw(to.getYaw());
						p.setBedSpawnLocation(abs, true);
						Func.sendActionbar(p, Func.format("&eCheckpoint"));
					}
				} else if (to.getBlock().getRelative(BlockFace.DOWN).getType() == Material.CYAN_WOOL) {
					p.setBedSpawnLocation(null);
				}
		}
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		// linked to NATURAL REGEN & NO DAMAGE BONUS ARROW
		if (e.getEntity() instanceof Player) {
			Player dmged = (Player) e.getEntity();
			if (e.getDamager() instanceof Player) {
				Player dmger = (Player) e.getDamager();
				if (Game.active() && (Game.pvp() || TeamMngr.teamOf(dmged) == TeamMngr.teamOf(dmger))) {
					putInCombat(dmged, dmger);
				} else
					e.setCancelled(true);
				
			} else if (e.getDamager() instanceof Arrow) {
				if (isBonusArrow((Arrow) e.getDamager())) e.setDamage(0);
			}
		}
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (Game.active())
			e.setCancelled(
					campBlockInteract(e.getBlock(), e.getPlayer())
					);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		// linked to CAMPS & ORE EXP
		if (Game.active()) {
			e.setCancelled(
					campBlockBreak(e.getBlock(), e.getPlayer())
					);
			oreExp(e.getBlock());
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		if (Game.active()) {
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
			else if (!Game.pvp() && Math.random() < 0.8)
				e.setCancelled(true);
	}
	
	/*
	 * ============================================================================
	 *                                  CAMP
	 * ============================================================================
	 */
	
	private static boolean campBlockBreak(Block b, Player p) {
		if (CampMngr.isFlag(b.getLocation().add(0.5, 0.5, 0.5)))
			return true;
		return campBlockInteract(b, p);
	}
	
	private static boolean campBlockInteract(Block b, Player p) {
		if (b.getType() != Material.TNT) {
			Team pteam = TeamMngr.teamOf(p);
			if (pteam != null)
				for (Camp camp : CampMngr.get())
					if (CampMngr.isInCamp(b.getLocation(), camp))
						if (!camp.getRivals().contains(pteam)) {
							Func.sendActionbar(p, Func.format("&cCe camp n'est pas à vous"));
							return true;
						}
		}
		return false;
	}
	
	private static void campArea(Player p) {
		// triggered whenever a player move to a new block in x.z plane
		if (!p.isInvisible()) {
			Team pteam = TeamMngr.teamOf(p);
			if (pteam != null)
				for (Camp camp : CampMngr.get())
					if (CampMngr.isInVitalSpace(p.getLocation(), camp))
						if (!camp.getRivals().contains(pteam)) {
							if (CampMngr.teamProtect(camp.getOwner(), camp))
								CampMngr.conquest(camp, pteam);
							else
								CampMngr.capture(camp, pteam);
							break;
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
			wolf.setCollarColor(TeamMngr.getDyeColor((Player) p));
			wolf.setCustomName(pickRandomName());
		}
	}
	
	private static List<String> names = new ArrayList<>(Arrays.asList(
			"Kylian", "Nutella", "Rexma", "Scooby", "Jean-Luc Mélenchon", "Alexis Corbière", "Bellatar", "Douglas", "Squeezie",
			"Fluffy", "Marex"
			));
	
	private static String pickRandomName() {
		int rng = (int) (Math.random() * names.size());
		String picked = names.get(rng);
		names.remove(rng);
		return picked;
	}
	
	// Avoid Wolf teleport while they are in Order Task
	@EventHandler
	public void onEntityTeleport(EntityTeleportEvent e) {
		if (e.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf)e.getEntity();
			if (DogAnger.contain(wolf) || ComeBack.contain(wolf))
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
							Func.glowFor((LivingEntity)dog, p, 10);
					}
					prevTarget.remove(p);
				} else {
					setAnger(dogs, target);
					p.getWorld().playSound(p.getLocation(), "custom.whistle", SoundCategory.PLAYERS, 2.0f, 1.0f);
					Func.glowFor(target, p, 10);
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
    	ents.removeAll(TeamMngr.getAliveTeamMembers(TeamMngr.teamOf(p)));
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
			DogAnger.addAnger(wolf, target);
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
			DogAnger.removeAnger(wolf);
			ComeBack.addDog(wolf, dummy, p);
		}
	}
	
	// Wolf Remove
	@EventHandler
	public void onEntityDeath(EntityDeathEvent e) {
		if (e.getEntity() instanceof Wolf) {
			Wolf wolf = (Wolf) e.getEntity();
			if (wolf.isTamed()) DogsMngr.removeWolf((Player) wolf.getOwner(), wolf);
			DogAnger.removeAnger(wolf);
			ComeBack.removeDog(wolf);
		}
		Wolf angry = DogAnger.getDogAngry(e.getEntity());
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
		if (Game.active() && p.isSneaking())
			if (e.getRightClicked() instanceof Wolf)
				if (DogsMngr.ownership((Wolf) e.getRightClicked(), p))
					if (TeamMngr.getAliveTeamMembers(TeamMngr.teamOf(p)).size() > 1)
						p.openInventory(GUIMngr.getDTM(p, e.getRightClicked().getCustomName()));
	}
	
	@EventHandler
	public void onPlayerToggleSneak(PlayerToggleSneakEvent e) {
		// /!\ optimize area of activation /!\
		if (!Game.active() && e.getPlayer().getLocation().getX() <= 8 && e.getPlayer().getGameMode() == GameMode.ADVENTURE)
			e.getPlayer().openInventory(GUIMngr.getTM());
	}
	
	Map<Material, Particle> particle = new HashMap<>();
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (!Game.active()) {
			if (e.getView().getTitle() == "Selection") {
				if (e.getCurrentItem() != null)
					GUIMngr.clickTM((Player) e.getWhoClicked(), e.getCurrentItem());
				e.setCancelled(true);
			} else if (e.getView().getTitle().equals("Trainées")) {
				if (e.getCurrentItem() != null)
					// addPlayer to particles
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
		return bTypeN.length() > 7 && (bTypeN.endsWith("ORE") || bTypeN.startsWith("RAW") || bTypeN.equals("ANCIENT_DEBRIS"));
	}
	
	private static void spawnExp(Block b) {
		ExperienceOrb exp = (ExperienceOrb) b.getWorld().spawnEntity(b.getLocation(), EntityType.EXPERIENCE_ORB);
		if (b.getType() != Material.ANCIENT_DEBRIS)
			exp.setExperience(oreToExp(b.getType().toString()));
		else
			exp.setExperience(70);
	}

	private static int oreToExp(String name) {
		int fact = 1;
		if (name.startsWith("RAW")) {
			name = name.substring(4, name.length() - 6);
			fact = 9;
		} else if (name.startsWith("DEEPSLATE")) {
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
			Main.map.playSound(bloc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
			Main.map.spawnParticle(Particle.SMOKE_LARGE, bloc, 8, 0.4, 0.4, 0.4, 0.01);
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
			if (b.getWorld() == Main.map && b.getLocation().getY() < 16)
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
		NaturalRegen.addPlayer(dmged);
		NaturalRegen.addPlayer(dmger);
	}
	
	@EventHandler
	public void onEntityDamage(EntityDamageEvent e) {
		if (e.getEntity() instanceof Player) {
			if (Game.active()) {
				if (e.getCause() != DamageCause.FIRE_TICK)
					NaturalRegen.addPlayer((Player) e.getEntity());
			} else if (e.getCause() == DamageCause.VOID)
				e.getEntity().remove();
		}
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (Game.active()) {
			if (e.getEntity() instanceof Player)
				if (NaturalRegen.isInCombat((Player) e.getEntity()))
					e.setCancelled(true);
		}
	}
	
	// ##### DEATH #####
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (Game.active()) {
			Player p = e.getPlayer();
			p.setGameMode(GameMode.SPECTATOR);
			p.teleport(Main.map.getSpawnLocation());
		}
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		if (Game.active()) {
			Player dead = e.getEntity();
			dead.setBedSpawnLocation(dead.getLocation());
			if (Game.pvp()) {
				Player killer = dead.getKiller();
				if (killer != null) {
					if (killer instanceof Player) {
						TeamMngr.teamKills(dead, killer);
						DogsMngr.transferDogsTo(dead, killer);
					}
				} else if (TeamMngr.getAliveTeamMembers(TeamMngr.teamOf(dead)).size() == 0) {
					TeamMngr.respawnAllKilled(TeamMngr.teamOf(dead));
				}
				EndGameMngr.winCondition(null);
				e.setDroppedExp(dead.getTotalExperience());
			} else
				RespawnCooldown.add(dead);
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
				Color color = TeamMngr.getColor(p);
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
		if (Game.active() && e.getCause() == TeleportCause.ENDER_PEARL) {
			Player p = (Player) e.getPlayer();
			p.setHealth(Math.round((p.getHealth()-0.1)/1.5));
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
		Player p = (Player) e.getPlayer();
		TeamMngr.remove(p);
		Wolf angry = DogAnger.getDogAngry((LivingEntity) e.getPlayer());
		if (angry != null) {
			Player owner = (Player) angry.getOwner();
			comeBack(DogsMngr.get().get(owner), owner);
		}
	}

}