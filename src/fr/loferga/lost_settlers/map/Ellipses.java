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
	
	private static boolean clear = false;

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
				final int lowx = (int) ploc.getX() - 50;
				final int dx = 100;
				final int lowy = (int) ploc.getY();
				final int dy = 70;
				final int lowz = (int) ploc.getZ() - 50;
				final int dz = 100;
				if (!clear) {
					System.out.println(MapMngr.highest_ground);
					for (LodeGenerator g : MapMngr.generators)
						System.out.println("mat: "+g.ore+" yd: "+g.yratio+" gf: "+g.gaussFactor+" go: "+g.gaussOffset+" sb: "+g.sizeBounds+" count: "+g.count);
					clear = true;
					return true;
				} else {
					for (int x = lowx-5; x<lowx+dx+5; x++)
						for (int y = lowy-5; y<lowy+dy+5; y++)
							for (int z = lowz-5; z<lowz+dz+5; z++) {
								Block b = new Location(Main.map, x, y, z).getBlock();
								if (b.getType()!=Material.AIR)
									b.setType(Material.AIR);
							}
					clear = false;
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
