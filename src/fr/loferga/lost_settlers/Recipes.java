package fr.loferga.lost_settlers;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Recipes {
	
	private static final String[][] N = new String[][] {
		new String[] {"short", "&ccourte "},
		new String[] {"middle", "&6moyenne "},
		new String[] {"long", "&elongue "}
	};
	
	private static final String[][] TN = new String[][] {
		new String[] {"", ""},
		new String[] {"flicker", "&escintillants "},
		new String[] {"trail", "&7persistents "},
		new String[] {"lure", "&bdistrayants "}
	};
	
	private static final Material[][] R = new Material[][] {
		new Material[] {},
		new Material[] {Material.GLOWSTONE_DUST},
		new Material[] {Material.COPPER_INGOT},
		new Material[] {Material.GLOWSTONE_DUST, Material.COPPER_INGOT}
	};
	
	private static  boolean getBitAt(char c, int i) {
		return (c & (1 << i)) != 0;
	}
	
	public static void createRecipes(Main main) {
		// Fireworks
		for (char t = 0; t<4; t++) {
			for (int gp = 1; gp<4; gp++) {
				for (int p = 1; p<4; p++) {
					Bukkit.addRecipe(getFireworkRecipe(
							p + N[gp-1][0] + "_range_" + TN[t][0] + "firework",
							Func.format("&rArtifices " + TN[t][1] + N[gp-1][1] + "&rportée"),
							gp, p, R[t], getBitAt(t, 0), getBitAt(t, 1)
							));
				}
			}
		}
		Bukkit.removeRecipe(NamespacedKey.minecraft("ender_chest"));
		Bukkit.removeRecipe(NamespacedKey.minecraft("shield"));
	}
	
	private static ShapelessRecipe getFireworkRecipe(String key, String name, int Gp, int P, Material[] recipe, boolean flicker, boolean trail) {
		NamespacedKey nk = new NamespacedKey(Main.getPlugin(Main.class), key);
		ItemStack i = new ItemStack(Material.FIREWORK_ROCKET, P * 4);
		FireworkMeta fm = (FireworkMeta) i.getItemMeta();
		fm.setDisplayName(name);
		Builder fe = FireworkEffect.builder();
		fe.flicker(flicker);
		fe.trail(trail);
		fe.withColor(Color.WHITE);
		fe.with(Type.BALL_LARGE);
		fm.addEffect(fe.build());
		fm.setPower(Gp);
		i.setItemMeta(fm);
		ShapelessRecipe fwr = new ShapelessRecipe(nk, i);
		fwr.addIngredient(Gp, Material.GUNPOWDER);
		fwr.addIngredient(P, Material.PAPER);
		for (Material mat : recipe)
			fwr.addIngredient(mat);
		return fwr;
	}
	
	public static FireworkMeta setColor(ItemMeta meta, Color color) {
		FireworkMeta fm = (FireworkMeta) meta;
		int power = fm.getPower();
		Builder fe = FireworkEffect.builder();
		fe.flicker(effectsContains(fm.getEffects(), true));
		fe.trail(effectsContains(fm.getEffects(), false));
		fe.withColor(color);
		fe.with(Type.BALL_LARGE);
		fm.clearEffects();
		fm.addEffect(fe.build());
		fm.setPower(power);
		return fm;
	}
	
	private static boolean effectsContains(List<FireworkEffect> effects, boolean flicker) {
		boolean contain = false;
		int i = 0, length = effects.size();
		while (i<length && !contain) {
			if (flicker) {
				if (effects.get(i).hasFlicker()) {
					contain = true;
				}
			} else {
				if (effects.get(i).hasTrail()) {
					contain = true;
				}
			}
			i++;
		}
		return contain;
	}
	
}
