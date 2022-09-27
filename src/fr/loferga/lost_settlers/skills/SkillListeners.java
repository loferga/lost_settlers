package fr.loferga.lost_settlers.skills;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.potion.PotionEffect;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.dogs.DogMngr;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.map.camps.Camp;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class SkillListeners implements Listener {
	
	private static final Material[] LOGS = new Material[] {
			Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG
			};
	
	private static final Material[] undesirable = new Material[] {
			Material.SAND, Material.RABBIT, Material.PORKCHOP, Material.CHORUS_FRUIT, Material.COD, Material.POTATO, Material.SALMON,
			Material.CHICKEN, Material.MUTTON, Material.BEEF, Material.IRON_PICKAXE
	};
	
	private static final List<FurnaceRecipe> FRECIPES = getAllFurnaceRecipes();
	private static List<FurnaceRecipe> getAllFurnaceRecipes() {
		List<FurnaceRecipe> res = new ArrayList<>();
		Iterator<Recipe> it = Bukkit.recipeIterator();
		while (it.hasNext()) {
			Recipe next = it.next();
			if (next instanceof FurnaceRecipe) {
				FurnaceRecipe frec = (FurnaceRecipe) next;
				Material imat = frec.getInput().getType();
				if (!(Func.primeContain(undesirable, imat) || Func.primeContain(LOGS, imat)))
					res.add(frec);
			}
		}
		return res;
	}
	
	public static void giveEquipment(Game g) {
		for (Player p : g.getPlayers()) {
			Skill s = SkillSelection.get(p);
			if (s == null) return;
			switch (s) {
			case DRESSAGE:
				giveDogs(p); break;
			case GLOUTON:
				giveGoldenApple(p); break;
			default:
				break;
			}
		}
	}
	
	
	// FORGEUR
	private static boolean forge = Func.primeContain(SkillSelection.getSkills(), Skill.FORGE);
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e) {
		if (!forge || SkillSelection.empty(Skill.FORGE)) return;
		if (SkillSelection.get(e.getPlayer()) != Skill.FORGE) return;
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		Player p = e.getPlayer();
		Collection<ItemStack> drops = e.getBlock().getDrops(p.getInventory().getItemInMainHand(), p);
		if (drops.size()>0) {
			e.setDropItems(false);
			for (ItemStack i : drops)
				p.getWorld().dropItemNaturally(e.getBlock().getLocation(), getFurnaceResult(i));
		}
	}
	
	private static ItemStack getFurnaceResult(ItemStack i) {
		for (FurnaceRecipe frec : FRECIPES)
			if (frec.getInput().getType() == i.getType())
				return frec.getResult();
		return i;
	}
	
	// BUCHERON
	private static final boolean abattage = Func.primeContain(SkillSelection.getSkills(), Skill.ABATTAGE);
	
	@EventHandler
	public void onBlockLogBreak(BlockBreakEvent e) {
		if (!abattage || SkillSelection.empty(Skill.ABATTAGE)) return;
		if (SkillSelection.get(e.getPlayer()) != Skill.ABATTAGE) return;
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if (!Func.primeContain(LOGS, e.getBlock().getType())) return;
		Location loc = e.getBlock().getLocation().add(0, -1, 0);
		if (!(Func.primeContain(LOGS, loc.getBlock().getType()) || loc.getBlock().getType() == Material.AIR)) return;
		loc.add(0, 2, 0);
		
		while (Func.primeContain(LOGS, loc.getBlock().getType())) {
			e.getPlayer().breakBlock(loc.getBlock());
			loc.add(0, 1, 0);
		}
	}
	
	// DRESSAGE
	
	private static final int DOG_N = 2;
	
	public static void giveDogs(Player p) {
		for (int i = 0; i<DOG_N; i++) {
			Wolf dog = (Wolf) p.getWorld().spawnEntity(p.getLocation(), EntityType.WOLF);
			dog.setTamed(true);
			DogMngr.addWolf(p, dog);
			DogMngr.setDogsColor(p, TeamMngr.teamOf(p).getDyeColor());
			// set dog name
		}
	}
	
	/*
	// FERMIER
	private static final boolean fermier = Func.primeContain(SkillSelection.getSkills(), Skill.FERMIER);
	
	private static ItemStack F_HOE = buildFHoe();
	private static ItemStack buildFHoe() {
		ItemStack hoe = new ItemStack(Material.IRON_HOE, 1);
		Damageable hoedmg = (Damageable) hoe.getItemMeta();
		hoedmg.setDamage(125);
		hoe.setItemMeta((ItemMeta) hoedmg);
		return hoe;
	}
	
	private static void giveHoe(Player p) {
		p.getInventory().addItem(F_HOE);
	}
	
	@EventHandler
	public void onWheatBreak(BlockBreakEvent e) {
		if (!fermier) return;
		if (SkillSelection.get((Player) e.getPlayer()) != Skill.FERMIER) return;
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if (e.getBlock().getType() != Material.WHEAT) return;
		Collection<ItemStack> drops = e.getBlock().getDrops();
		for (ItemStack i : drops)
			if (i.getType() == Material.WHEAT)
				e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(0.5, 0.5, 0.5), i);
	}
	
	// commented part don't work, (don't apply in-game)
	@EventHandler
	public void onFertilizeCrop(BlockFertilizeEvent e) {
		if (!fermier) return;
		if (SkillSelection.get((Player) e.getPlayer()) != Skill.FERMIER) return;
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if (e.getBlocks().size() != 1) return;
		
		Block b = e.getBlock();
		if (!(b.getBlockData() instanceof Ageable)) return;
		Ageable crop = (Ageable) b.getBlockData();
		crop.setAge(crop.getMaximumAge());
		b.setBlockData(crop);
		
	}
	*/
	
	// GLOUTON
	private static final boolean glouton = Func.primeContain(SkillSelection.getSkills(), Skill.GLOUTON);
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent e) {
		if (!glouton || SkillSelection.empty(Skill.GLOUTON)) return;
		if (SkillSelection.get((Player) e.getEntity()) != Skill.GLOUTON) return;
		
		e.setFoodLevel(2 * e.getEntity().getFoodLevel());
	}
	
	@EventHandler
	public void onPotionEffect(EntityPotionEffectEvent e) {
		if (!glouton || SkillSelection.empty(Skill.GLOUTON)) return;
		if (!(e.getEntity() instanceof Player)) return;
		Player p = (Player) e.getEntity();
		if (SkillSelection.get(p) != Skill.GLOUTON) return;
		
		PotionEffect eff = e.getNewEffect();
		int duration = eff.getDuration();
		p.addPotionEffect(new PotionEffect(eff.getType(), duration + (int)((1/5)*duration), eff.getAmplifier(), true, true));
		e.setCancelled(true);
	}
	
	public static void giveGoldenApple(Player p) {
		p.getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 1));
	}
	
	// DEMOLISSEUR
	private static final boolean demolition = Func.primeContain(SkillSelection.getSkills(), Skill.DEMOLITION);
	
	@EventHandler
	public void onTNTBreak(BlockBreakEvent e) {
		if (!demolition || SkillSelection.empty(Skill.DEMOLITION)) return;
		if (SkillSelection.get(e.getPlayer()) != Skill.DEMOLITION) return;
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if (e.getBlock().getType() != Material.TNT) return;
		
		e.setDropItems(false);
		TNTPrimed tnt = (TNTPrimed) e.getBlock().getWorld().spawnEntity(e.getBlock().getLocation().add(0.5, 0.5, 0.5), EntityType.PRIMED_TNT);
		tnt.setSource(e.getPlayer());
		tnt.setIsIncendiary(true);
	}
	
	@EventHandler
	public void onTNTExplode(EntityExplodeEvent e) {
		if (!(e.getEntity() instanceof TNTPrimed)) return;
		
		TNTPrimed tnt = (TNTPrimed) e.getEntity();
		if (!tnt.isIncendiary()) return;
			
		e.getLocation().getWorld().createExplosion(e.getLocation(), 5.2f, false, true, tnt.getSource());
		e.setYield(1.0f);
		e.setCancelled(true);
	}
	
	// ROUBLARD
	private static final boolean roublard = Func.primeContain(SkillSelection.getSkills(), Skill.ROUBLARD);
	
	@EventHandler
	public void onFallingDamages(EntityDamageEvent e) {
		if (!roublard || SkillSelection.empty(Skill.ROUBLARD)) return;
		if (!(e.getEntity() instanceof Player)) return;
		if (e.getCause() != DamageCause.FALL) return ;
		Game game = GameMngr.gameIn((Player) e.getEntity());
		if (game == null) return;
		
		Camp c = game.campIn(e.getEntity().getLocation());
		if (c == null || c.getOwner() != TeamMngr.teamOf((Player) e.getEntity())) return;
		
		e.setCancelled(true);
	}
	
	// PISTAGE
	private static final boolean pistage = Func.primeContain(SkillSelection.getSkills(), Skill.PISTAGE);
	
	private static Set<Player> inAir = new HashSet<>();
	
	@EventHandler
	public void onPlayerEnterNewBlock(PlayerMoveEvent e) {
		if (!pistage || SkillSelection.empty(Skill.PISTAGE)) return;
		Location from = e.getFrom();
		Location to = e.getTo();
		Player p = e.getPlayer();
		
		if (inAir.contains(p)) {inAir.remove(p); new Footprint(p); return;}
		if ((int)from.getX() == (int)to.getX() && (int)from.getY() == (int)to.getY() && (int)from.getZ() == (int)to.getZ()) return;
		
		if (((Entity) p).isOnGround()) {
			inAir.add(p);
		} else new Footprint(p);
	}
	
	// PRECISION
	private static final boolean precision = Func.primeContain(SkillSelection.getSkills(), Skill.PRECISION);

	@EventHandler
	public void onPlayerShootWithBow(ProjectileLaunchEvent e) {
		if (!precision || SkillSelection.empty(Skill.PRECISION)) return;
		
		Projectile proj = e.getEntity();
		
		if (!(proj.getShooter() instanceof Player)) return;
		Player p = (Player) proj.getShooter();
		if (SkillSelection.get(p) != Skill.PRECISION) return;
		if (p.getGameMode() == GameMode.CREATIVE) return;
		
		if (!(proj instanceof Arrow)) return;
		
		double pow = proj.getVelocity().length();
		proj.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(1.5 * pow));
		
		Arrow arr = (Arrow) proj;
		AbstractArrow abarr = (AbstractArrow) arr;
		abarr.setKnockbackStrength((int) (1.5 * abarr.getKnockbackStrength()));
	}
	
}