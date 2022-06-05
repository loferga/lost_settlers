package fr.loferga.lost_settlers.gui;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.dogs.DogsMngr;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class GUIMngr {
	
	protected static final ItemStack[] T_ITEMS = buildTeamsItem();
	
	protected static final ItemStack BLANK = getItem(Material.BLACK_STAINED_GLASS_PANE, " ");
	
	protected static final Inventory TEAM_MENU = buildTM(2, 2);
	
	public static ItemStack[] buildTeamsItem() {
		LSTeam[] teams = TeamMngr.get();
		ItemStack[] items = new ItemStack[teams.length];
		for (int i = 0; i<teams.length; i++) {
			items[i] = getItem(teams[i].getFlag(), teams[i].getName());
		}
		return items;
	}
	
	private static Inventory getBlankInventory(int n, String name) {
		Inventory blankInv = Bukkit.createInventory(null, n, name);
		for (int i = 0; i<n; i++) {
			blankInv.setItem(i, BLANK);
		}
		return blankInv;
	}
	
	public static Inventory getTM() {
		Inventory inv = Bukkit.createInventory(null, 9, "Selection");
		inv.setContents(TEAM_MENU.getContents());
		int[] teamSize = TeamMngr.teamsSizes(Main.hub);
		int i = 0, j, length = T_ITEMS.length;
		while (i < length) {
			j = 1;
			while (j < teamSize[i]) {
				inv.addItem(T_ITEMS[i]);
				j++;
			}
			i++;
		}
		return inv;
	}
	
	public static void clickTM(Player p, ItemStack item) {
		if (item.getItemMeta().getDisplayName() != " ") {
			LSTeam[] teams = TeamMngr.get();
			boolean stop = false;
			int i = 0, length = teams.length;
			while (i<length && !stop) {
				LSTeam team = teams[i];
				if (team.getFlag() == item.getType()) {
					TeamMngr.join(p, team);
					GUIMngr.refreshTM();
					stop = true;
				}
				i++;
			}
		}
	}
	
	public static Inventory getDTM(Player p, String wn) {
		Game game = GameMngr.gameIn(p);
		List<Player> pl = game.getAliveTeamMates(p);
		List<Wolf> dogsl = DogsMngr.get().get(p);
		int i = 0, j = 0, dllen = dogsl.size(), pllen = pl.size();
		Inventory inv = getBlankInventory(18 + (pllen/9), "Confier " + wn);
		while (j<pllen || i<dllen) {
			if (j<pllen) {
				inv.setItem(j, getPlayerHead(pl.get(j)));
			}
			if (i<dllen) {
				Wolf dog = dogsl.get(i);
				inv.setItem((inv.getSize() - i - 1), getDogNameTag(dog, dog.getCustomName().equals(wn)));
			}
			i++; j++;
		}
		return inv;
	}
	
	private static ItemStack getPlayerHead(Player p) {
		ItemStack head = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta meta = (SkullMeta) head.getItemMeta();
		meta.setOwningPlayer(p);
		meta.setDisplayName(p.getName());
		head.setItemMeta(meta);
		return head;
	}
	
	private static ItemStack getDogNameTag(Wolf w, boolean isSelected) {
		ItemStack tag = new ItemStack(Material.NAME_TAG);
		ItemMeta meta = tag.getItemMeta();
		meta.setDisplayName(w.getCustomName());
		if (isSelected) {
			meta.addEnchant(Enchantment.SOUL_SPEED, 0, true);
			meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		}
		tag.setItemMeta(meta);
		return tag;
	}
	
	public static void clickDTM(Player p, ItemStack item) {
		if (item.getItemMeta().getDisplayName() != " ") {
			Material mat = item.getType();
			if (mat == Material.PLAYER_HEAD) {
				DogsMngr.transferDogTo(
						DogsMngr.getDogByName(p, p.getOpenInventory().getTitle().substring(8)),
						p,
						((SkullMeta) item.getItemMeta()).getOwningPlayer().getPlayer()
						);
				if (DogsMngr.get().containsKey(p)) {
					List<Wolf> dogs = DogsMngr.get().get(p);
					p.openInventory(getDTM(p, dogs.get((int) (Math.random() * dogs.size())).getCustomName()));
					p.updateInventory();
				} else
					p.closeInventory();
			} else if (mat == Material.NAME_TAG) {
				p.openInventory(getDTM(p, item.getItemMeta().getDisplayName()));
				p.updateInventory();
			}
		}
	}
	
	private static ItemStack getItem(Material mat, String name) {
		ItemStack i = new ItemStack(mat);
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(name);
		i.setItemMeta(im);
		return i;
	}
	
	private static Inventory buildTM(int a, int b) {
		Inventory teamMenu = getBlankInventory(9, "Selection");
		for (int i = 0; i<3; i++) {
			teamMenu.setItem(a*i + b, T_ITEMS[i]);
		}
		return teamMenu;
	}
	
	public static void refreshTM() {
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getOpenInventory() != null)
				if (p.getOpenInventory().getTitle().equals("Selection")) {
					p.openInventory(getTM());
					p.updateInventory();
				}
	}
	
	public static void refreshDTM() {
		for (Player p : Bukkit.getOnlinePlayers())
			if (p.getOpenInventory() != null)
				if (p.getOpenInventory().getTitle().substring(0, 7).equals("Confier")) {
					p.updateInventory();
				}
	}
	
}
