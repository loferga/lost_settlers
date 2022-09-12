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
			Func.format("&3Mineur"),
			List.of(
					Func.format("&6Talent :"), Func.format("&5Cuit tout ce qu'il casse")
					)
			),
	BUCHERON(
			Material.IRON_AXE,
			Func.format("&3Bucheron"),
			List.of(
					Func.format("&6Talent :"), Func.format("&5Fait tomber les arbres"),
					Func.format("&6Equipement :"), Func.format("&5Hache en fer amochée")
					)
			),
	DEMOLISSEUR(
			Material.TNT,
			Func.format("&3Demolisseur"),
			List.of(
					Func.format("&6Talent :"), Func.format("&5Augmente la puissance des explosifs")
					)
			),
	CLAIRVOYANT(
			Material.ENDER_EYE,
			Func.format("&3Clairvoyant"),
			List.of(
					Func.format("&6Talent :"), Func.format("&5Fait briller les ennemies visibles"), Func.format("&5autour du porteur")
					)
			),
	ARCHER(
			Material.BOW,
			Func.format("&3Archer"),
			List.of(
					Func.format("&6Talent :"), Func.format("&5Tire droit mais inflige moins")
					)
			),
	NULL(
			null,
			null,
			new ArrayList<>()
			);
	
	private Material item;
	private String name;
	private List<String> lore;
	
	Skill(Material i, String n, List<String> l) {
		item = i;
		name = n;
		lore = l;
	}

	public ItemStack getMarkedItem() {
		ItemStack i = new ItemStack(item, 1);
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(name);
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