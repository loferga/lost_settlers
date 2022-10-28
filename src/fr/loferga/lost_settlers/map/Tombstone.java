package fr.loferga.lost_settlers.map;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import fr.loferga.lost_settlers.util.Func;

public class Tombstone {
	
	private Block block;
	private ItemStack[] items;
	private int xp;
	
	public Tombstone(PlayerDeathEvent e) {
		block = e.getEntity().getLocation().getBlock();
		block.setType(Material.SKELETON_SKULL);
		List<ItemStack> drops = e.getDrops();
		items = new ItemStack[drops.size()];
		for (int i = 0; i<items.length; i++)
			items[i] = Func.pop(drops);
		xp = e.getDroppedExp();
	}
	
	public boolean isTombstone(Block broke) {
		return block.equals(broke);
	}
	
	public void drop() {
		Location loc = block.getLocation();
		World w = loc.getWorld();
		for (ItemStack i : items)
			w.dropItemNaturally(loc, i);
		Func.dropExp(loc, xp);
	}
	
}