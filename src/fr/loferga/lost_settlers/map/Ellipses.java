package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import fr.loferga.lost_settlers.Func;
import fr.loferga.lost_settlers.Main;

public class Ellipses implements TabExecutor {
	
	private static int stage = 0;

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return new ArrayList<>();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (sender instanceof Player) {
			Player p = (Player) sender;
			if (args.length == 0) {
				final Location ploc = p.getLocation();
				final int lowx = (int) ploc.getX() - 40;
				final int dx = 80;
				final int lowy = (int) ploc.getY();
				final int dy = 70;
				final int lowz = (int) ploc.getZ() - 40;
				final int dz = 80;
				if (stage == 0) {
					for (int x = lowx; x<lowx+dx; x++)
						for (int y = lowy; y<lowy+dy; y++)
							for (int z = lowz; z<lowz+dz; z++) {
								Block b = new Location(Main.map, x, y, z).getBlock();
								if (b.getType() != Material.STONE)
									b.setType(Material.STONE);
							}
					stage += 1;
					return true;
				} else if (stage == 1) {
					for (LodeGenerator g : MapMngr.generators)
						MapMngr.createLode(g, new double[] {lowx, lowy, lowz}, new double[] {lowx+dx, lowy+dy, lowz+dz});
					stage += 1;
					return true;
				} else if (stage == 2) {
					for (int x = lowx; x<lowx+dx; x++)
						for (int y = lowy; y<lowy+dy; y++)
							for (int z = lowz; z<lowz+dz; z++) {
								Block b = new Location(Main.map, x, y, z).getBlock();
								if (b.getType() == Material.STONE)
									b.setType(Material.AIR);
							}
					stage += 1;
					return true;
				} else if (stage == 3) {
					for (int x = lowx; x<lowx+dx; x++)
						for (int y = lowy; y<lowy+dy; y++)
							for (int z = lowz; z<lowz+dz; z++) {
								Block b = new Location(Main.map, x, y, z).getBlock();
								if (b.getType()!=Material.AIR)
									b.setType(Material.AIR);
							}
					stage = 0;
					return true;
				}
			}
			sendInvalid(p);
		}
		return false;
	}
	
	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/ellipses"));
	}

}
