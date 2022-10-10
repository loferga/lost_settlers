package fr.loferga.lost_settlers.map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import fr.loferga.lost_settlers.map.geometry.*;
import fr.loferga.lost_settlers.util.Func;

public class Lode {
	
	private static Material[] valid_stone = {Material.STONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DIRT,
				Material.GRAVEL};
	
	public Lode(World world, Material ore, Point O, Vector i,Vector j, Vector k) {
		this.world = world;
		this.ore = ore;
		this.O = O;
		this.i = i;
		this.j = j;
		this.k = k;
	}
	
	private World world;
	private Material ore;
	private Point O;
	private Vector i;
	private Vector j;
	private Vector k;
	
	public boolean isInLode(double[] pos) {
		// coordonnées relatives de X au point O
		Vector OX = O.vectorTo(pos[0], pos[1], pos[2]);
		// matrice de passage Pbb'
		Matrix P = new Matrix(new double[][] {
			{i.x, j.x, k.x},
			{i.y, j.y, k.y},
			{i.z, j.z, k.z}
		});
		// on Xb = Pbb' * Xb' = Xb' = Xb/Pbb' = Xb' = Xb * Pbb'^-1 donc Xl = P.inverse().multiply(OX)
		Point Xl = P.inverse().multiply(OX.toMatrix()).toPoint();
		// .toMatrix() là pour satisfaire la forme générale d'un produit de matrice, de même pour .toPoint()
		return Math.sqrt(Xl.x()*Xl.x() + Xl.y()*Xl.y() + Xl.z()*Xl.z()) <= 1;
	}
	
	public void setMaterial() {
		double[] maxv = i.values();
		for (double[] compound : new double[][] {j.values(), k.values()})
			for (int i = 0; i<3; i++)
				if (compound[i]>maxv[i])
					maxv[i] = compound[i];
		
		double[] bounds = new double[] {
				((int) O.x() - maxv[0])-1.5, ((int) O.x() + maxv[0])+1.5,
				((int) O.y() - maxv[1])-1.5, ((int) O.y() + maxv[1])+1.5,
				((int) O.z() - maxv[2])-1.5, ((int) O.z() + maxv[2])+1.5
		};
		
		for (double x = bounds[0]; x < bounds[1]; x+=1.0)
			for (double y = bounds[2]; y < bounds[3]; y+=1.0)
				for (double z = bounds[4]; z < bounds[5]; z+=1.0) {
					if (isInLode(new double[] {x, y, z})) {
						Block b = new Location(world, x, y, z).getBlock();
						if (Func.primeContain(valid_stone, b.getType())) {
							b.setType(ore);
						} else if (b.getType() == Material.DEEPSLATE || b.getType() == Material.TUFF) {
							if (ore.toString().endsWith("_ORE"))
								b.setType(Material.valueOf("DEEPSLATE_".concat(ore.toString())));
							else
								b.setType(ore);
						}
					}
				}
	}

}