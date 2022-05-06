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
import fr.loferga.lost_settlers.map.geometry.Point;
import fr.loferga.lost_settlers.map.geometry.Vector;

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
				final int lowx = (int) ploc.getX() - 40;
				final int dx = 80;
				final int lowy = (int) ploc.getY();
				final int dy = 60;
				final int lowz = (int) ploc.getZ() - 40;
				final int dz = 80;
				if (!clear) {
					for (int n = 0; n<30; n++) {
						double y = Func.onBounds(0, 1, Func.gauss(5)*1.3) * dy + lowy;
						Vector[] ijk = randomVectors(2, 4.5, 1, 3.5, 1, 1.5);
						new Lode(Material.COAL_ORE, new Point(
								random(lowx, lowx + dx),
								y,
								random(lowz, lowz + dz)
						), ijk[0], ijk[1], ijk[2]).setMaterial();
					}
					for (int n = 0; n<20; n++) {
						double y = Func.onBounds(0, 1, Func.gauss(4)*1.1) * dy + lowy;
						Vector[] ijk = randomVectors(1.5, 3.5, 1, 2.5, 1, 1.5);
						new Lode(Material.IRON_ORE, new Point(
								random(lowx, lowx + dx),
								y,
								random(lowz, lowz + dz)
						), ijk[0], ijk[1], ijk[2]).setMaterial();
					}
					for (int n = 0; n<15; n++) {
						double y = Func.onBounds(0, 1, Func.gauss(2)*0.8) * (dy-20) + lowy;
						Vector[] ijk = randomVectors(1.5, 3.5, 1, 2.5, 1, 1.5);
						new Lode(Material.GOLD_ORE, new Point(
								random(lowx, lowx + dx),
								y,
								random(lowz, lowz + dz)
						), ijk[0], ijk[1], ijk[2]).setMaterial();
					}
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
	
	private static Vector[] randomVectors(double imin, double imax, double jmin, double jmax, double kmin, double kmax) {
		Vector[] ijk = new Vector[3];
		double ilen = random(imin, imax);
		double jlen = random(jmin, jmax);
		double klen = random(kmin, kmax);
		Vector i = new Vector(Math.random()*2-1, Math.random()*2-1, Math.random()*2-1).normalize().multiply(ilen);
		ijk[0] = i;
		Vector j = new Vector(ijk[0].y(), -ijk[0].x(), 0).normalize().multiply(jlen);
		ijk[1] = j;
		ijk[2] = new Vector(i.y()*j.z() - i.z()*j.y(), i.z()*j.x() - i.x()*j.z(), i.x()*j.y() - i.y()*j.x()).normalize().multiply(klen);
		return ijk;
	}
	
	private static double random(double min, double max) {
		return Math.random() * (max-min) + min;
	}
	
	private static void sendInvalid(Player p) {
		p.sendMessage(Func.format("&cInvalid usage, please use:\n/ellipses"));
	}

}
