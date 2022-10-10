package fr.loferga.lost_settlers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.EnchantingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import fr.loferga.lost_settlers.dogs.Anger;
import fr.loferga.lost_settlers.dogs.DogMngr;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.game.MobMngr;
import fr.loferga.lost_settlers.gui.GUIMngr;
import fr.loferga.lost_settlers.map.MapMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.rules.Wounded;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;
import fr.loferga.lost_settlers.util.Func;

public class Listeners implements Listener {
	
	@EventHandler
	public void onJoin(final PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if (p.getWorld() != MapMngr.HUB) {GUIMngr.giveSelector(p); return;}
		if (TeamMngr.teamOf(p) != null) return;
			TeamMngr.join(p, TeamMngr.NULL);
	}
	
	// events with multiples effects
	@EventHandler
	public void onPlayerMove(PlayerMoveEvent e) {
		// linked to CAMPS & MAGMA CHAMBER
		Player p = e.getPlayer();
		Game g = GameMngr.gameIn(p);
		if (g == null) return;
		if (!g.pvp() || p.getGameMode() != GameMode.SURVIVAL) return;

		Location from = e.getFrom();
		Location to = e.getTo();
		if ((int)from.getX() != (int)to.getX() || (int)from.getZ() != (int)to.getZ())
			campArea(p);
		else if (MapMngr.getMapSettings(from.getWorld()).isChamberActive() && (int)from.getY() != (int)to.getY() && g.isInChamber(to))
			g.addPlayerInChamber(p);
		
	}
	
	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
		// linked to NATURAL REGEN & NO DAMAGE BONUS ARROW
		if (!(e.getEntity() instanceof Player)) return;
		
		Player dmged = (Player) e.getEntity();
		Entity src = e.getDamager();
		
		if (src instanceof Player) {
			
			Player dmger = (Player) e.getDamager();
			Game game = GameMngr.gameIn(dmger);
			if (game != null && (game.pvp() || TeamMngr.teamOf(dmged) == TeamMngr.teamOf(dmger))) {
				putInCombat(dmged, dmger);
			} else
				e.setCancelled(true);
				
		} else if (src instanceof Arrow) {
			
			if (isBonusArrow((Arrow) e.getDamager())) e.setDamage(0);
			if (src instanceof SpectralArrow) {
				
				SpectralArrow sa = (SpectralArrow) e.getDamager();
				if (sa.getShooter() instanceof Player)
					Func.glowFor(dmged, TeamMngr.teamOf((Player) sa.getShooter()).getPlayers(), 600);
				
			}
				
		} else if (src instanceof Monster)
			
			e.setDamage(0.35 * e.getFinalDamage());
		
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		if (e.getBlock().getType() == Material.TNT) return;
		
		e.setCancelled(
				campBlockInteract(e.getBlock(), e.getPlayer())
				);
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		// linked to CAMPS & ORE EXP
		if (e.getBlock().getType() == Material.TNT) return;
			
		if (GameMngr.gameIn(e.getPlayer()) == null) return;
		
		if (campBlockBreak(e.getBlock(), e.getPlayer()))
			e.setCancelled(true);
		else oreExp(e.getBlock());
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent e) {
		Game game = GameMngr.gameIn(e.getPlayer());
		if (game != null) {
			if (e.getAction() == Action.LEFT_CLICK_AIR) {
				DogMngr.onOrder(e);
			} else if (e.getAction() == Action.RIGHT_CLICK_BLOCK && e.hasItem() && e.getItem().getType() != Material.TNT)
				e.setCancelled(campBlockBreak(e.getClickedBlock(), e.getPlayer()));
		} else if (e.getAction() == Action.RIGHT_CLICK_AIR
				|| (e.getAction() == Action.RIGHT_CLICK_BLOCK && !e.getClickedBlock().getType().isInteractable())) {
			
			if (e.getItem() == null || !GUIMngr.isSelector(e.getItem())) return;
			
			e.getPlayer().openInventory(GUIMngr.getTM(e.getPlayer()));
			
		}
	}
	
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent e) {
		Location loc = e.getLocation();
		Game game = GameMngr.getGame(loc.getWorld());
		if (game == null || !(e.getEntity() instanceof Monster)) return;
		
		if (game.getMapSettings().isChamberActive() && game.isInChamber(loc))
			spawnMagmaCube(loc);
		
		if (game.getMapSettings().isLodesActive()) {
			double ratio = game.undergroundLevel(loc);
			if (ratio<=1)
				MobMngr.setProperties(e.getEntity(), Math.abs(1-ratio), game.isInChamber(loc));
		}
		
	}
	
	/*
	 * ============================================================================
	 *                                  CAMP
	 * ============================================================================
	 */
	
	private static boolean campBlockBreak(Block b, Player p) {
		Game game = GameMngr.gameIn(p);
		if (game.isFlag(b))
			return true;
		return campBlockInteract(b, p);
	}
	
	private static boolean campBlockInteract(Block b, Player p) {
		Game game = GameMngr.gameIn(p);
		LSTeam pteam = TeamMngr.teamOf(p);
		if (game == null || pteam == null) return false;
		Camp camp = game.campIn(b.getLocation());
		if (camp == null || camp.getRivals().contains(pteam)) return false;
		
		Func.sendActionbar(p, Func.format("&cCe camp n'est pas à vous"));
		return true;
	}
	
	private static void campArea(Player p) {
		// triggered whenever a player move to a new block in x.z plane
		if (p.isInvisible()) return;
		Game game = GameMngr.gameIn(p);
		LSTeam pteam = TeamMngr.teamOf(p);
		if (game == null || pteam == null) return;
		Camp camp = game.vitalIn(p.getLocation());
		if (camp == null || camp.getRivals().contains(pteam)) return;
		
		if (game.teamProtect(camp.getOwner(), camp))
			game.conquest(camp, pteam);
		else
			game.capture(camp, pteam);
	}
	
	/*
	 * ============================================================================
	 *                                     GUI
	 * ============================================================================
	 */
	
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		if (GameMngr.gameIn((Player) e.getView().getPlayer()) == null) {
			if (e.getCurrentItem() == null) return;
			
			if (e.getView().getTitle() == "Selection") {
				GUIMngr.clickTM((Player) e.getWhoClicked(), e.getCurrentItem());
				e.setCancelled(true);
			} else if (e.getView().getTitle() == "Talents") {
				boolean res = GUIMngr.clickSM((Player) e.getWhoClicked(), e.getCurrentItem());
				if (res) e.getWhoClicked().openInventory(GUIMngr.getTM((Player) e.getWhoClicked()));
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
	// ##### XP ######
	
	final static int X = 2;
	
	private static void dropExp(Location loc, int xpAmount) {
		int r = xpAmount;
		int x = X;
		while (r > 0) {
			int xp;
			if (r >= x) {
				xp = x;
				r -= x;
			} else {
				xp = r;
				r = 0;
			}
			ExperienceOrb orb = (ExperienceOrb) loc.getWorld().spawnEntity(loc, EntityType.EXPERIENCE_ORB);
			orb.setExperience(xp);
			x *= X;
		}
	}
	
	// ##### ORES #####
	
	private void oreExp(Block b) {
		if (!isOre(b.getType().toString())) return;
		
		dropExp(b.getLocation(), oreToExp(b.getType().toString()));
	}
	
	private static boolean isOre(String bn) {
		return bn.length() > 7 && (bn.endsWith("ORE") || bn.equals("ANCIENT_DEBRIS"));
	}
	
	private static int C = 3, Fe = 4, Cu = 5, Au = 6, R = 8, L = 12, D = 14, E = 20, A = 70,
			DSF = 2;

	private static int oreToExp(String name) {
		int fact = 1;
		if (name.startsWith("DEEPSLATE")) {
			name = name.substring(10, name.length() - 4);
			fact = DSF;
		} else
			name = name.substring(0, name.length() - 4);
		switch (name) {
		case "COAL": return C * fact;
		case "IRON": return Fe * fact;
		case "COPPER": return Cu * fact;
		case "GOLD": return Au * fact;
		case "REDSTONE": return R * fact;
		case "LAPIS": return L * fact;
		case "DIAMOND": return D * fact;
		case "EMERALD": return E * fact;
		case "ANCIENT_DE": return A;
		default: return 0;
		}
	}
	
	// ##### MAGMA CHAMBER #####
	
	@EventHandler
	public void onObsidianForm(BlockFormEvent e) {
		if (e.getNewState().getType() != Material.OBSIDIAN) return;
		
		Location bloc = e.getBlock().getLocation().add(0.5, 0.5, 0.5);
		bloc.getWorld().playSound(bloc, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f);
		bloc.getWorld().spawnParticle(Particle.SMOKE_LARGE, bloc, 8, 0.4, 0.4, 0.4, 0.01);
		e.getBlock().setType(Material.STONE);
		e.setCancelled(true);
	}

	@EventHandler
	public void vaporizeWater(BlockPhysicsEvent e) {
		Block b = e.getBlock();
		if (b.getType() != Material.WATER) return;
		Game game = GameMngr.getGame(b.getWorld());
		if (game == null) return;
		
		if (game.isInChamber(b.getLocation()))
			b.setType(Material.AIR);
	}
	
	private static void spawnMagmaCube(Location loc) {
		MagmaCube magma = (MagmaCube) loc.getWorld().spawnEntity(loc, EntityType.MAGMA_CUBE);
		magma.setSize((int) (Math.random() * 4));
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
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if (GameMngr.gameIn(p) == null) return;
		if (e.getCause() == DamageCause.FIRE_TICK) return;
		
		Wounded.addPlayer(p);
	}
	
	@EventHandler
	public void onEntityRegainHealth(EntityRegainHealthEvent e) {
		if (!(e.getEntity() instanceof Player) || !(Wounded.isInCombat((Player) e.getEntity()))) return;
		
		e.setCancelled(true);
	}
	
	// ##### DEATH #####
	
	@EventHandler
	public void onPlayerRespawn(PlayerRespawnEvent e) {
		if (GameMngr.gameIn(e.getPlayer()) == null) return;
		
		Player p = e.getPlayer();
		p.setGameMode(GameMode.SPECTATOR);
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {
		Game game = GameMngr.gameIn(e.getEntity());
		if (game == null) return;
		
		Player dead = e.getEntity();
		dead.setBedSpawnLocation(dead.getLocation(), true);
		if (game.pvp()) {
			Player killer = dead.getKiller();
			if (killer != null) {
				if (killer instanceof Player) {
					game.kill(dead, killer);
					DogMngr.transferDogsTo(dead, killer);
				}
			}
			else
				game.winCondition(null, null);
			e.setDroppedExp(0);
			dropExp(dead.getLocation(), dead.getTotalExperience());
		} else
			game.addRespawn(dead);
		GUIMngr.refreshDTM();
	}
	
	// ##### CRAFTS COLOR ASSIGNMENT #####
	
	@EventHandler
	public void onPlayerCraftItem(PrepareItemCraftEvent e) {
		ItemStack result = e.getInventory().getResult();
		if (result == null || result.getType() != Material.FIREWORK_ROCKET) return;
		
		Player p = (Player) e.getView().getPlayer();
		Color color = TeamMngr.teamOf(p).getColor();
		result.setItemMeta((ItemMeta) Recipes.setColor(result.getItemMeta(), color));
		e.getInventory().setResult(result);
	}
	
	// ##### NETHERITE SMITHING #####
	
	@EventHandler
	public void onPlayerSmithItem(PrepareSmithingEvent e) {
		ItemStack compound = e.getInventory().getItem(1);
		if (compound == null || compound.getType() != Material.NETHERITE_INGOT) return;
		if (e.getInventory().getItem(0) == null || e.getResult() == null) return;
		
		ItemMeta im = e.getResult().getItemMeta();
		Map<Enchantment, Integer> enchantments = im.getEnchants();
		for (Enchantment ench : enchantments.keySet())
			im.addEnchant(ench, enchantments.get(ench) + 1, true);
		e.getResult().setItemMeta(im);
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
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		PotionEffect eff = e.getNewEffect();
		if (e.getNewEffect() == null || e.getNewEffect().isAmbient()) return;  // not clearing effect && not already modified
		
		String effN = e.getNewEffect().getType().getName();
		if (effN.equals("INVISIBILITY")) {                                                            // no particles
			p.addPotionEffect(new PotionEffect(eff.getType(), eff.getDuration(), eff.getAmplifier(), true, false));
			e.setCancelled(true);
		} else if (effN.equals("FIRE_RESISTANCE")) {                   // decreased duration
			p.addPotionEffect(new PotionEffect(eff.getType(), (int) (80 + eff.getDuration()/25), eff.getAmplifier(), true, true));
			e.setCancelled(true);
		} else if (effN.equals("INCREASE_DAMAGE")) {                   // decreased duration
			p.addPotionEffect(new PotionEffect(eff.getType(), (int) (60 + eff.getDuration()/20), eff.getAmplifier(), true, true));
			e.setCancelled(true);
		}
		// TODO replace strength vanilla effect with a percent damage increase (not flat) with EntityDamagedByEntityEvent
	}
	
	// ##### TEAM CHAT #####
	
	@EventHandler
	public void onPlayerSendMessage(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		Game game = GameMngr.gameIn(p);
		if (game == null) return;
		
		LSTeam pt = game.getTeam(p);
		for (Player player : game.getWorld().getPlayers()) {
			LSTeam playert = game.getTeam(player);
			if (playert == null || playert == pt)
				player.sendMessage(ChatColor.GRAY + "<" + p.getName() + "> " + e.getMessage());
		}
		e.setCancelled(true);
	}
	
	// FLAGS
	
	@EventHandler (priority = EventPriority.HIGHEST)
	public void onEntityExplode(EntityExplodeEvent e) {
		Game game = GameMngr.getGame(e.getLocation().getWorld());
		if (game == null) return;
		
		e.setYield(1.0f);
		for (Block b : e.blockList())
			if (game.isFlag(b)) {
				e.setCancelled(true);
				Entity src = e.getEntity();
				if (e.getEntity() instanceof TNTPrimed)
					src = ((TNTPrimed) e.getEntity()).getSource();
				e.getLocation().getWorld().createExplosion(e.getLocation(), 4f, false, false, src);
				return;
			}
	}
	
	// ENCHANTMENTS
	
	@EventHandler
	public void onEnchantImprove(EnchantItemEvent e) {
		ItemStack it = e.getItem();
		if (it.getEnchantments().isEmpty()) return;
		
		e.getEnchantsToAdd().clear();
		Map<Enchantment, Integer> iEnch = it.getEnchantments();
		int b = e.whichButton();
		for (Enchantment enchant : iEnch.keySet())
			if (b-- == 0)
				e.getEnchantsToAdd().put(enchant, it.getEnchantmentLevel(enchant) + 1);
				//it.addEnchantment(enchant, it.getEnchantmentLevel(enchant) + 1);
	}
	
	@EventHandler
	public void onEnchantPrepareOnEnchanted(PrepareItemEnchantEvent e) {
		Map<Enchantment, Integer> enchants = ((EnchantingInventory) e.getView().getTopInventory()).getItem().getEnchantments();
		if (enchants.isEmpty()) return;
		
		e.setCancelled(false);
		EnchantmentOffer[] offers = e.getOffers();
		int i = 0;
		for (Enchantment enchant : enchants.keySet()) {
			int level = enchants.get(enchant);
			if (level != enchant.getMaxLevel())
				offers[i++] = new EnchantmentOffer(enchant, level + 1, 3 * level);
		}
		while (i<3)
			offers[i++] = null;
	}
	
	// BREWING
	
	@EventHandler
	public void onBrew(BrewEvent e) {
		if (e.getFuelLevel()==0)
			e.getContents().setFuel(new ItemStack(Material.BLAZE_POWDER, 1));
	}
	
	@EventHandler
	public void onBrewingInventoryOpen(InventoryOpenEvent e) {
		if (e.getInventory().getType() != InventoryType.BREWING) return;
		if (!(e.getInventory() instanceof BrewerInventory)) return;
		
		BrewerInventory binv = (BrewerInventory) e.getInventory();
		if (binv.getHolder().getFuelLevel() == 0)
			binv.setFuel(new ItemStack(Material.BLAZE_POWDER, 1));
	}
	
	@EventHandler
	public void onMoveItem(InventoryClickEvent e) {
		if (e.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY
				&& e.getAction() != InventoryAction.PLACE_ALL
				&& e.getAction() != InventoryAction.PLACE_ONE) return;
		ItemStack i = e.getCurrentItem();
		if (i.getType() != Material.POTION) return;
		if (!i.hasItemMeta() || ((PotionMeta) i.getItemMeta()).getBasePotionData().getType() != PotionType.WATER) return;
		
		PotionMeta pm = (PotionMeta) i.getItemMeta();
		pm.setBasePotionData(new PotionData(PotionType.AWKWARD));
		i.setItemMeta(pm);
	}
	
	// EXIT CASE
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		MapMngr.spawnTeleport(e.getPlayer());
		TeamMngr.remove(e.getPlayer());
		Set<Wolf> angry = Anger.getDogAngryAt((LivingEntity) e.getPlayer());
		if (angry == null) return;
		
		for (Wolf w : angry) {
			Player ow = (Player) w.getOwner();
			Anger.removeAnger(w);
			DogMngr.comeBack(List.of(w), ow);
		}
	}

}