package fr.loferga.lost_settlers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld;
import org.bukkit.entity.Player;

import net.minecraft.core.BlockPosition;
import net.minecraft.world.level.block.entity.TileEntityBeacon;

public class LSTest implements TabExecutor{
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 0) {
				activate(p.getLocation().getBlock());
				return true;
			}
			sendInvalid(p);
		}
		return false;
	}
	
	private void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid Usage, please use: /lstest"));
	}
	
	private static void activate(Block block) {
		block.setType(Material.BEACON);
		TileEntityBeacon beacon = (TileEntityBeacon) ((CraftWorld) block
				.getWorld()).getHandle().getTileEntity(new BlockPosition(block.getX(),
				block.getY(), block.getZ()));
		Field activek = null;
		Field activel = null;
		try {
			int i = 1;
			activek = TileEntityBeacon.class.getDeclaredField("k");
			activek.setAccessible(true);
			activek.set(beacon, i);
			activel = TileEntityBeacon.class.getDeclaredField("l");
			activel.setAccessible(true);
			activel.set(beacon, i);
		}
		catch (NoSuchFieldException e) {
			e.printStackTrace();
		}
		catch (SecurityException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
		catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
}