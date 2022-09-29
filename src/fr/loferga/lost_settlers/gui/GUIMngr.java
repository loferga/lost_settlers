package fr.loferga.lost_settlers.gui;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Game;
import fr.loferga.lost_settlers.Main;
import fr.loferga.lost_settlers.dogs.DogMngr;
import fr.loferga.lost_settlers.game.GameMngr;
import fr.loferga.lost_settlers.skills.Skill;
import fr.loferga.lost_settlers.skills.SkillSelection;
import fr.loferga.lost_settlers.teams.LSTeam;
import fr.loferga.lost_settlers.teams.TeamMngr;

public class GUIMngr {
	
	private static Plugin plg = Main.getPlugin(Main.class);
	
	private static final NamespacedKey selectorKey = new NamespacedKey(plg, "selector");
	
	private static final ItemStack SELECTOR = buildSelector();
	private static ItemStack buildSelector() {
		ItemStack sel = new ItemStack(Material.COMPASS);
		ItemMeta selm = sel.getItemMeta();
		selm.setDisplayName(Func.format("&6Selection"));
		selm.getPersistentDataContainer().set(
				selectorKey, PersistentDataType.BYTE, (byte) 1
			);
		sel.setItemMeta(selm);
		return sel;
	}
	
	public static boolean isSelector(ItemStack i) {
		return i.getItemMeta().getPersistentDataContainer().has(selectorKey, PersistentDataType.BYTE);
	}
	
	public static void giveSelector(Player p) {
		p.getInventory().setItem(8, SELECTOR);
	}
	
	private static final ItemStack[] T_ITEMS = buildTeamsItem();
	private static ItemStack[] buildTeamsItem() {
		LSTeam[] teams = TeamMngr.get();
		ItemStack[] items = new ItemStack[teams.length];
		for (int i = 0; i<teams.length; i++) {
			items[i] = getItem(teams[i].getFlag(), teams[i].getName());
		}
		return items;
	}
	
	private static final ItemStack[] S_ITEMS = buildSkillsItem();
	private static ItemStack[] buildSkillsItem() {
		Skill[] skills = SkillSelection.getSkills();
		ItemStack[] items = new ItemStack[skills.length];
		
		int i = 0;
		for (Skill s : skills)
			items[i++] = s.getMarkedItem();
		
		return items;
	}
	
	private static ItemStack getItem(Skill s) {
		for (ItemStack i : S_ITEMS) {
			PersistentDataContainer pdc = i.getItemMeta().getPersistentDataContainer();
			if (pdc.get(new NamespacedKey(plg, "Skill"), PersistentDataType.STRING) == s.toString())
				return i;
		}
		return null;
	}
	
	private static final ItemStack BLANK = getItem(Material.BLACK_STAINED_GLASS_PANE, " ");
	private static final ItemStack LINE = getItem(Material.GRAY_STAINED_GLASS_PANE, " ");
	
	private static final int[][] slots = new int[][] {
		new int[] {4},
		new int[] {3, 5},
		new int[] {2, 4, 6},
		new int[] {1, 3, 5, 7},
		new int[] {0, 2, 4, 6, 8},
		new int[] {1 ,2, 3, 5, 6, 7},
		new int[] {1, 2, 3, 4, 5, 6, 7},
		new int[] {0, 1, 2, 3, 5, 6, 7, 8}
	};
	
	private static final Inventory TEAM_MENU = buildTM();
	private static Inventory buildTM() {
		final int len = T_ITEMS.length;
		final int q = (int) (len/9)+1;
		Inventory teamMenu = getBlankInventory((q+2)*9, "Selection");
		fillLine(teamMenu, q);
		int i = 0, j, t = 0;
		while (i<q) {
			j = 0;
			if (i == q-1) {
				while (j<len%9) {
					teamMenu.setItem((i*9)+slots[(len%9)-1][j], T_ITEMS[t]);
					j++;
					t++;
				}
			} else {
				while (j<9) {
					teamMenu.setItem((i*9)+j, T_ITEMS[t]);
					j++;
					t++;
				}
			}
			i++;
		}
		return teamMenu;
	}
	
	private static final Inventory SKILL_MENU = buildSM();
	private static Inventory buildSM() {
		final int len = S_ITEMS.length;
		final int q = (int) (len/9)+1;
		Inventory skillMenu = getBlankInventory(q*9, "Talents");
		int i = 0, j, t = 0;
		while (i<q) {
			j = 0;
			if (i == q-1) {
				while (j<len%9) {
					skillMenu.setItem((i*9)+slots[(len%9)-1][j], S_ITEMS[t]);
					j++;
					t++;
				}
			} else {
				while (j<9) {
					skillMenu.setItem((i*9)+j, S_ITEMS[t]);
					j++;
					t++;
				}
			}
			i++;
		}
		return skillMenu;
	}
	
	private static Inventory getBlankInventory(int n, String name) {
		Inventory blankInv = Bukkit.createInventory(null, n, name);
		for (int i = 0; i<n; i++)
			blankInv.setItem(i, BLANK);
		return blankInv;
	}
	
	private static void fillLine(Inventory inv, int n) {
		int max = (n+1)*9;
		for (int i = n*9; i<max; i++) {
			inv.setItem(i, LINE);
		}
	}
	
	// ### INVENTORY CRAFTING ###
	
	public static Inventory getTM(Player p) {
		Inventory inv = Bukkit.createInventory(null, TEAM_MENU.getSize(), "Selection");
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
		Skill s = SkillSelection.get(p);
		ItemStack si = getItem(Material.ENDER_PEARL, Func.format("&cPas de Talent"));
		ItemMeta im = si.getItemMeta();
		Skill.NULL.mark(im);
		si.setItemMeta(im);
		if (s != null)
			si = getItem(s);
		inv.setItem(TEAM_MENU.getSize()-5, si);
		return inv;
	}
	
	public static Inventory getDTM(Player p, String wn) {
		Game game = GameMngr.gameIn(p);
		List<Player> pl = game.getAliveTeamMates(p);
		List<Wolf> dogsl = DogMngr.get().get(p);
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
	
	// ### CLICK EVENTS ###
	
	public static void clickTM(Player p, ItemStack item) {
		if (item.getItemMeta().getDisplayName().equals(" ")) return;
		
		if (item.getItemMeta().getPersistentDataContainer().has(
				new NamespacedKey(Main.getPlugin(Main.class), "Skill"), PersistentDataType.STRING)
				) {
			p.openInventory(SKILL_MENU);
			return;
		}
		
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
	
	public static boolean clickSM(Player p, ItemStack item) {
		if (item.getItemMeta().getDisplayName().equals(" ")) return false;
		
		String name = item.getItemMeta().getPersistentDataContainer().get(
				new NamespacedKey(Main.getPlugin(Main.class), "Skill"), PersistentDataType.STRING);
		SkillSelection.select(p, name);
		return true;
	}
	
	public static void clickDTM(Player p, ItemStack item) {
		if (item.getItemMeta().getDisplayName() != " ") {
			Material mat = item.getType();
			if (mat == Material.PLAYER_HEAD) {
				DogMngr.transferDogTo(
						DogMngr.getDogByName(p, p.getOpenInventory().getTitle().substring(8)),
						p,
						((SkullMeta) item.getItemMeta()).getOwningPlayer().getPlayer()
						);
				if (DogMngr.get().containsKey(p)) {
					List<Wolf> dogs = DogMngr.get().get(p);
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
	
	// ### REFRESH ###
	
	public static void refreshTM() {
		for (Player p : Main.hub.getPlayers())
			if (p.getOpenInventory() != null)
				if (p.getOpenInventory().getTitle().equals("Selection")) {
					p.openInventory(getTM(p));
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
	
	// ### MISC ###
	
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
	
	private static ItemStack getItem(Material mat, String name) {
		ItemStack i = new ItemStack(mat);
		ItemMeta im = i.getItemMeta();
		im.setDisplayName(name);
		i.setItemMeta(im);
		return i;
	}
	
}
