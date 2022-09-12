package fr.loferga.lost_settlers.skills;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Game;

public class SkillMngr implements Listener {
	
	// FORGEUR
	private static boolean forgeur = Func.primeContain(SkillSelection.getSkills(), Skill.FORGEUR);
	
	// IMPROVE !!!
	private static final List<FurnaceRecipe> FRECIPES = getAllFurnaceRecipes();
	private static List<FurnaceRecipe> getAllFurnaceRecipes() {
		List<FurnaceRecipe> res = new ArrayList<>();
		Iterator<Recipe> it = Bukkit.recipeIterator();
		while (it.hasNext()) {
			Recipe next = it.next();
			if (next instanceof FurnaceRecipe)
				res.add((FurnaceRecipe) next);
		}
		return res;
	}
	
	private static final Material[] LOGS = new Material[] {
			Material.ACACIA_LOG, Material.BIRCH_LOG, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.OAK_LOG, Material.SPRUCE_LOG
			};
	
	@EventHandler
	public void onBlockBreakM(BlockBreakEvent e) {
		if (!forgeur) return;
		if (SkillSelection.get(e.getPlayer()) != Skill.FORGEUR) return;
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		Player p = e.getPlayer();
		Collection<ItemStack> drops = e.getBlock().getDrops(p.getInventory().getItemInMainHand(), p);
		if (drops.size()>0) {
			e.setDropItems(false);
			for (ItemStack i : drops)
				p.getWorld().dropItem(e.getBlock().getLocation().add(0.5, 0.5, 0.5), getFurnaceResult(i));
		}
	}
	
	private static ItemStack getFurnaceResult(ItemStack i) {
		if (i.getType() == Material.COBBLESTONE || i.getType() == Material.SAND) return i;
		for (FurnaceRecipe frec : FRECIPES)
			if (frec.getInput().getType() == i.getType())
				return frec.getResult();
		return i;
	}
	
	// BUCHERON
	private static final boolean bucheron = Func.primeContain(SkillSelection.getSkills(), Skill.BUCHERON);
	
	@EventHandler
	public void onBlockBreakB(BlockBreakEvent e) {
		if (!bucheron) return;
		if (SkillSelection.get(e.getPlayer()) != Skill.BUCHERON) return;
		if (e.getPlayer().getGameMode() == GameMode.CREATIVE) return;
		
		if (!Func.primeContain(LOGS, e.getBlock().getType())) return;
		
		Location loc = e.getBlock().getLocation().add(0, 1, 0);
		while (Func.primeContain(LOGS, loc.getBlock().getType())) {
			e.getPlayer().breakBlock(loc.getBlock());
			loc.add(0, 1, 0);
		}
	}
	
	private static final ItemStack B_AXE = buildBAxe();
	private static ItemStack buildBAxe() {
		ItemStack axe = new ItemStack(Material.IRON_AXE, 1);
		Damageable axedmg = (Damageable) axe.getItemMeta();
		axedmg.setDamage(150);
		axe.setItemMeta((ItemMeta) axedmg);
		return axe;
	}
	
	public static void giveAxeB(Game g) {
		for (Player p : g.getPlayers())
			if (SkillSelection.get(p) == Skill.BUCHERON)
				p.getInventory().addItem(B_AXE);
	}
	
	// ARCHER
	private static final boolean archer = Func.primeContain(SkillSelection.getSkills(), Skill.ARCHER);

	@EventHandler
	public void onPlayerLaunch(ProjectileLaunchEvent e) {
		if (!archer) return;
		
		Projectile proj = e.getEntity();
		
		if (!(proj.getShooter() instanceof Player)) return;
		Player p = (Player) proj.getShooter();
		if (SkillSelection.get(p) != Skill.ARCHER) return;
		
		if (!(proj instanceof Arrow)) return;
		
		double pow = proj.getVelocity().length();
		proj.setVelocity(p.getEyeLocation().getDirection().normalize().multiply(1.2 * pow));
		
		Arrow arr = (Arrow) proj;
		arr.setDamage(0.75* arr.getDamage());
	}
	
	// DEMOLISSEUR
	private static final boolean demolisseur = Func.primeContain(SkillSelection.getSkills(), Skill.DEMOLISSEUR);
	
	@EventHandler
	public void onBlockBreakD(BlockBreakEvent e) {
		if (!demolisseur) return;
		if (SkillSelection.get(e.getPlayer()) != Skill.DEMOLISSEUR) return;
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
	
}