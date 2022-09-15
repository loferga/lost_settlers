package fr.loferga.lost_settlers.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;

public enum Skill {
	
	// RESSOURCES
	FORGEUR(
			Material.GOLDEN_PICKAXE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Cuit tout ce qu'il casse")
					)
			),
	BUCHERON(
			Material.IRON_AXE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Fait tomber les arbres"),
					Func.format("&6Equipement :"), Func.format("&5Hache en fer amochée")
					)
			),
	CHASSEUR(
			Material.BONE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Commence la partie avec un chien")
					)
			),
	FERMIER(
			Material.IRON_HOE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Rammasse plus de récoltes"),
					Func.format("&6Equipement :"), Func.format("&5Faux en fer amochée")
					)
			),
	GLOUTON(
			Material.COOKED_CHICKEN,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Ne subis pas la faim"),
					Func.format("&6Equipement :"), Func.format("&5Pomme en or")
					)
			),
	// CAMPS
	DEMOLISSEUR(
			Material.TNT,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Augmente la puissance des explosifs")
					)
			),
	ROUBLARD(
			Material.FEATHER,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Insensible aux dommages de chute"), Func.format("Sauf dans les campements ennemis")
					)
			),
	// COMBAT
	CLAIRVOYANT(
			Material.ENDER_EYE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Fait briller les ennemies visibles"), Func.format("&5autour du porteur")
					)
			),
	ARCHER(
			Material.BOW,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Tire droit mais inflige moins")
					)
			),
	
	NULL(
			null,
			new ArrayList<>()
			);
	
	private Material item;
	private List<String> lore;
	
	Skill(Material i, List<String> l) {
		item = i;
		lore = l;
	}

	public ItemStack getMarkedItem() {
		ItemStack i = new ItemStack(item, 1);
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(Func.format("&3" + Func.toReadable(toString(), 1)));
		if (!lore.isEmpty()) im.setLore(lore);
		mark(im);
		i.setItemMeta(im);
		return i;
	}
	
	private static Plugin plg = Main.getPlugin(Main.class);
	
	public void mark(ItemMeta im) {
		im.getPersistentDataContainer().set(
				new NamespacedKey(plg, "Skill"), PersistentDataType.STRING, toString()
				);
	}
	
}