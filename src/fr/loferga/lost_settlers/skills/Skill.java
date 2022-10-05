package fr.loferga.lost_settlers.skills;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.util.Func;

public enum Skill {
	
	// RESSOURCES
	FORGE(
			Material.GOLDEN_PICKAXE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Cuire ce qui est détruit")
					)
			),
	ABATTAGE(
			Material.IRON_AXE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Faire s'abattre les arbres"),
					Func.format("&6Equipement :"), Func.format("&5Hache en fer amochée")
					)
			),
	DRESSAGE(
			Material.BONE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Commencer la parties avec deux chiens")
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
					Func.format("&6Talent :"), Func.format("&5Ingestion efficace"),
					Func.format("&6Equipement :"), Func.format("&5Pomme en or")
					)
			),
	// CAMPS
	DEMOLITION(
			Material.TNT,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Explosifs améliorés")
					)
			),
	ARTIFICE(
			Material.FIREWORK_ROCKET,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Feux d'artifices améliorés"),
					Func.format("6Equipement :"), Func.format("&5Poudre à canon")
					)
			),
	ROUBLARD(
			Material.FEATHER,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Insensibilité aux dommages de chute"), Func.format("&5Sauf dans les campements ennemis")
					)
			),
	// COMBAT
	CLAIRVOYANCE(
			Material.ENDER_EYE,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Faire briller les ennemies visibles")
					)
			),
	PISTAGE(
			Material.LEATHER_BOOTS,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Voir les traces de pas")
					)
			),
	PRECISION(
			Material.BOW,
			List.of(
					Func.format("&6Talent :"), Func.format("&5Tirer droit et plus vite")
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