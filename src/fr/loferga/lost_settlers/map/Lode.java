package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import fr.loferga.lost_settlers.map.geometry.*;
import fr.loferga.lost_settlers.util.Func;

public class Lode {
	
	private static final Material[] VALID_STONE = {Material.STONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DIRT,
				Material.GRAVEL};
	
	public Lode(World world, Material ore, Point o, Vector i,Vector j, Vector k) {
		this.world = world;
		this.ore = ore;
		this.o = o;
		p = new Matrix(new double[][] {
			{i.x, j.x, k.x},
			{i.y, j.y, k.y},
			{i.z, j.z, k.z}
		});
		p.inverse();
	}
	
	private World world;
	private Material ore;
	private Point o;
	private Matrix p; // matrice de passage Pbb'
	
	public boolean isInLode(double[] pos) {
		// coordonnées relatives de X au point O
		Vector ox = o.vectorTo(pos[0], pos[1], pos[2]);
		// on Xb = Pbb' * Xb' = Xb' = Xb/Pbb' = Xb' = Xb * Pbb'^-1 donc Xl = P.inverse().multiply(OX)
		Point xl = p.multiply(ox.toMatrix()).toPoint();
		// .toMatrix() là pour satisfaire la forme générale d'un produit de matrice, de méme pour .toPoint()
		return Math.sqrt(xl.x*xl.x + xl.y*xl.y + xl.z*xl.z) <= 1;
	}
	
	private static List<double[]> file = new ArrayList<>();
	
	public void setMaterial() {
		
		Set<double[]> passage = new HashSet<>();
		
		file.add(o.coords());
		
		while (!file.isEmpty()) {
			double[] pos = Func.pop(file);
			if (passage.add(pos)
			&& isInLode(new double[] {pos[0], pos[1], pos[2]})) {
				Block b = new Location(world, pos[0], pos[1], pos[2]).getBlock();
				if (Func.primeContain(VALID_STONE, b.getType())) {
					b.setType(ore);
					addFaces(pos);
				} else if (b.getType() == Material.DEEPSLATE || b.getType() == Material.TUFF) {
					if (ore.toString().endsWith("_ORE"))
						b.setType(Material.valueOf("DEEPSLATE_".concat(ore.toString())));
					else
						b.setType(ore);
					addFaces(pos);
				}
			}
		}
		
//		double[] maxv = i.values();
//		for (double[] compound : new double[][] {j.values(), k.values()})
//			for (int i = 0; i<3; i++)
//				if (compound[i]>maxv[i])
//					maxv[i] = compound[i];
//		
//		double[] bounds = new double[] {
//				((int) O.x - maxv[0])-1.5, ((int) O.x + maxv[0])+1.5,
//				((int) O.y - maxv[1])-1.5, ((int) O.y + maxv[1])+1.5,
//				((int) O.z - maxv[2])-1.5, ((int) O.z + maxv[2])+1.5
//		};
//		
//		for (double x = bounds[0]; x < bounds[1]; x+=1.0)
//			for (double y = bounds[2]; y < bounds[3]; y+=1.0)
//				for (double z = bounds[4]; z < bounds[5]; z+=1.0) {
//					if (isInLode(new double[] {x, y, z})) {
//						Block b = new Location(world, x, y, z).getBlock();
//						if (Func.primeContain(valid_stone, b.getType())) {
//							b.setType(ore);
//						} else if (b.getType() == Material.DEEPSLATE || b.getType() == Material.TUFF) {
//							if (ore.toString().endsWith("_ORE"))
//								b.setType(Material.valueOf("DEEPSLATE_".concat(ore.toString())));
//							else
//								b.setType(ore);
//						}
//					}
//				}
	}
	
	private static void addFaces(double[] pos) {
		file.add(new double[] {pos[0] + 1, pos[1]    , pos[2]    });
		file.add(new double[] {pos[0] - 1, pos[1]    , pos[2]    });
		file.add(new double[] {pos[0]    , pos[1] + 1, pos[2]    });
		file.add(new double[] {pos[0]    , pos[1] - 1, pos[2]    });
		file.add(new double[] {pos[0]    , pos[1]    , pos[2] + 1});
		file.add(new double[] {pos[0]    , pos[1]    , pos[2] - 1});
	}

}