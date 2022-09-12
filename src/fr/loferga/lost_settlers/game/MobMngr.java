package fr.loferga.lost_settlers.game;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;
import io.netty.util.internal.ThreadLocalRandom;

public class MobMngr {
	
	private static FileConfiguration cfg = Main.getPlugin(Main.class).getConfig();
	/*
	private static final int ARTIFACT_CHANCE = cfg.getInt("mobs.equipment.artifact.chance");
	private static final double ENCHANT_CHANCE = cfg.getDouble("mobs.equipment.enchantment.chance");
	private static final double ENCHANT_UP_CHANCE = cfg.getDouble("mobs.equipment.enchantment.up_chance");
	private static final int ENCHANT_AMOUNT = cfg.getInt("mobs.equipment.enchantment.amount");
	*/
	private static final float ARMOR_DC = (float) cfg.getDouble("mobs.equipment.armor.drop_chance");
	private static final double ARMOR_CHANCE = cfg.getDouble("mobs.equipment.armor.chance");
	private static final float WEAPON_DC = (float) cfg.getDouble("mobs.equipment.weapon.drop_chance");
	private static final double WEAPON_CHANCE = cfg.getDouble("mobs.equipment.weapon.chance");
	private static final double EFFECT_CHANCE = cfg.getDouble("mobs.effect.chance");
	private static final double EFFECT_UP_CHANCE = cfg.getDouble("mobs.effect.up_chance");
	private static final int EFFECT_AMOUNT = cfg.getInt("mobs.effect.amount");
	
	private static final Material[] HELMETS = new Material[] {
			Material.CHAINMAIL_HELMET, Material.GOLDEN_HELMET,
			Material.IRON_HELMET,Material.DIAMOND_HELMET, Material.NETHERITE_HELMET
			};
	private static final Material[] CHESTPLATES = new Material[] {
			Material.CHAINMAIL_CHESTPLATE, Material.GOLDEN_CHESTPLATE,
			Material.IRON_CHESTPLATE, Material.DIAMOND_CHESTPLATE, Material.NETHERITE_CHESTPLATE
			};
	private static final Material[] LEGGINGS = new Material[] {
			Material.CHAINMAIL_LEGGINGS, Material.GOLDEN_LEGGINGS,
			Material.IRON_LEGGINGS, Material.DIAMOND_LEGGINGS, Material.NETHERITE_LEGGINGS
			};
	private static final Material[] BOOTS = new Material[] {
			Material.CHAINMAIL_BOOTS, Material.GOLDEN_BOOTS,
			Material.IRON_BOOTS, Material.DIAMOND_BOOTS, Material.NETHERITE_BOOTS
			};
	private static final Material[] MELEE_WEAPONS = new Material[] {
			Material.STONE_SWORD, Material.STONE_AXE, Material.GOLDEN_SWORD, Material.GOLDEN_AXE,
			Material.IRON_SWORD, Material.IRON_AXE, Material.DIAMOND_SWORD, Material.DIAMOND_AXE,
			Material.NETHERITE_SWORD, Material.NETHERITE_AXE
			};
	
	private static final double[] ARMOR_CHANCES = new double[] {
			ARMOR_CHANCE, Math.pow(ARMOR_CHANCE, 2), Math.pow(ARMOR_CHANCE, 3), Math.pow(ARMOR_CHANCE, 4), Math.pow(ARMOR_CHANCE, 5)
			};
	private static final double[] WEAPON_CHANCES = new double[] {
			WEAPON_CHANCE, Math.pow(WEAPON_CHANCE, 2), Math.pow(WEAPON_CHANCE, 3), Math.pow(WEAPON_CHANCE, 4), Math.pow(WEAPON_CHANCE, 5),
			Math.pow(WEAPON_CHANCE, 6), Math.pow(WEAPON_CHANCE, 7), Math.pow(WEAPON_CHANCE, 8), Math.pow(WEAPON_CHANCE, 9), Math.pow(WEAPON_CHANCE, 10)
			};
	
	private static final PotionEffectType[] ALLOWED_POTION_EFFECT_TYPE = getAllowedPotionEffectTypes();
	private static PotionEffectType[] getAllowedPotionEffectTypes() {
		// get all the allowed potion effect types in temp, an excessively large array
		PotionEffectType[] temp = new PotionEffectType[PotionEffectType.values().length];
		int j = 0;
		for (PotionEffectType pet : PotionEffectType.values()) {
			String path = "mobs.effect.allowed." + pet.getName().toLowerCase();
			// potion effect type is allowed only if it's written in config.yml and if it's boolean value is true
			if (cfg.contains(path) && cfg.getBoolean(path)) {
				temp[j] = pet;
				j++;
			}
		}
		// then clean the temp array to get res array
		PotionEffectType[] res = new PotionEffectType[j];
		for (int i = 0; i<j; i++)
			res[i] = temp[i];
		return res;
	}
	
	public static void setProperties(Entity ent, double ratio, boolean artifact) {
		LivingEntity livEnt = (LivingEntity) ent;
		
		double rr = Math.pow(ratio, 4);
		
		if (ent instanceof Creeper) {
			Creeper c = (Creeper) ent;
			c.setExplosionRadius((int) ((1+ratio) * c.getExplosionRadius()));
		}
		
		// Equipment concern only Zombie an Skeleton
		if (ent instanceof Zombie || ent instanceof Skeleton) {
			EntityEquipment eq = livEnt.getEquipment();
			eq.setHelmetDropChance(ARMOR_DC);
			eq.setChestplateDropChance(ARMOR_DC);
			eq.setLeggingsDropChance(ARMOR_DC);
			eq.setBootsDropChance(ARMOR_DC);
			eq.setItemInMainHandDropChance(WEAPON_DC);
			eq.setItemInOffHandDropChance(WEAPON_DC);
			
			// helmet
			int hlvl = 0;
			double rngh = ThreadLocalRandom.current().nextDouble();
			while (rngh < rr * ARMOR_CHANCES[hlvl] && hlvl<4)
				hlvl++;
			// helmet enchant
			eq.setHelmet(new ItemStack(HELMETS[hlvl], 1));
			
			// chestplate
			int clvl = 0;
			double rngc = ThreadLocalRandom.current().nextDouble();
			while (rngc < rr * ARMOR_CHANCES[clvl] && clvl<4)
				clvl++;
			// chestplate enchant
			eq.setChestplate(new ItemStack(CHESTPLATES[clvl], 1));
			
			// leggings
			int llvl = 0;
			double rngl = ThreadLocalRandom.current().nextDouble();
			while (rngl < rr * ARMOR_CHANCES[llvl] && llvl<4)
				llvl++;
			// leggings enchant
			eq.setLeggings(new ItemStack(LEGGINGS[llvl], 1));
			
			// boots
			int blvl = 0;
			double rngb = ThreadLocalRandom.current().nextDouble();
			while (rngb < rr * ARMOR_CHANCES[blvl] && blvl<4)
				blvl++;
			// leggings enchant
			eq.setBoots(new ItemStack(BOOTS[blvl], 1));
			
			int wlvl = 0;
			if (ent instanceof Zombie) {
				// weapon
				double rngw = ThreadLocalRandom.current().nextDouble();
				while (rngw < rr * WEAPON_CHANCES[wlvl] && wlvl<9)
					wlvl++;
				// weapon enchant
				eq.setItemInMainHand(new ItemStack(MELEE_WEAPONS[wlvl], 1));
			}
			
		}
		
		// PotionEffects concern every Monster
		if (ALLOWED_POTION_EFFECT_TYPE.length > 0)
			for (int i = 0; i<EFFECT_AMOUNT; i++)
				if (ThreadLocalRandom.current().nextDouble() < rr * EFFECT_CHANCE) {
					int amplifier = 1;
					while (ThreadLocalRandom.current().nextDouble() < rr * EFFECT_UP_CHANCE)
						amplifier++;
					livEnt.addPotionEffect(new PotionEffect(
							Func.pickRandom(ALLOWED_POTION_EFFECT_TYPE),
							Integer.MAX_VALUE,
							amplifier
							));
				}
		
	}
	
}