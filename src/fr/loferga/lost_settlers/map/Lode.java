package fr.loferga.lost_settlers.map;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import fr.loferga.lost_settlers.map.geometry.Matrix;
import fr.loferga.lost_settlers.map.geometry.Point;
import fr.loferga.lost_settlers.map.geometry.Vector;
import fr.loferga.lost_settlers.util.Func;

public class Lode {
	
	private static final Material[] VALID_STONE = {Material.STONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.DIRT,
				Material.GRAVEL};
	
	public Lode(World world, Material ore, Point o, Vector i,Vector j, Vector k) {
		this.world = world;
		this.ore = ore;
		this.o = o;
//		this.i = i;
//		this.j = j;
//		this.k = k;
		p = new Matrix(new double[][] {
			{i.x, j.x, k.x},
			{i.y, j.y, k.y},
			{i.z, j.z, k.z}
		}).inverse();
	}
	
	private World world;
	private Material ore;
	private Point o;
//	private Vector i;
//	private Vector j;
//	private Vector k;
	private Matrix p; // matrice de passage Pbb'
	
	public boolean isInLode(double[] pos) {
		// coordonnées relatives de X au point O
		Vector ox = o.vectorTo(pos[0], pos[1], pos[2]);
		// on Xb = Pbb' * Xb' = Xb' = Xb/Pbb' = Xb' = Xb * Pbb'^-1 donc Xl = P.inverse().multiply(OX)
		Point xl = p.multiply(ox.toMatrix()).toPoint();
		// .toMatrix() là pour satisfaire la forme générale d'un produit de matrice, de méme pour .toPoint()
		return Math.sqrt(xl.x*xl.x + xl.y*xl.y + xl.z*xl.z) <= 1;
	}
	
	public void setMaterial() {
		
		List<int[]> file = new ArrayList<>();
		List<int[]> passage = new ArrayList<>();
		
		setBlock(o.coords());
		addFaces(new int[] {(int) o.x, (int) o.y, (int) o.z}, file);
		
		while (!file.isEmpty()) {
			int[] pos = Func.pop(file);
			double[] loc = new double[] {pos[0] + 0.5, pos[1] + 0.5, pos[2] + 0.5};
			if (addPosition(pos, passage)  // Set.add return false if pos is already contained in the set but == operand don't work on arrays
					&& isInLode(new double[] {loc[0], loc[1], loc[2]}) ) {
				setBlock(loc);
				addFaces(pos, file);
				
			}
		}
		
//		Material sign;
//		switch (ore) {
//		case COAL_ORE:
//			sign = Material.DARK_OAK_SIGN;
//			break;
//		case IRON_ORE:
//			sign = Material.BIRCH_SIGN;
//			break;
//		case COPPER_ORE:
//			sign = Material.ACACIA_SIGN;
//			break;
//		case DIAMOND_ORE:
//			sign = Material.OAK_SIGN;
//			break;
//		case REDSTONE_ORE:
//			sign = Material.CRIMSON_SIGN;
//			break;
//		case LAPIS_ORE:
//			sign = Material.WARPED_SIGN;
//			break;
//		default:
//			sign = Material.JUNGLE_SIGN;
//		}
//		new Location(world, o.coords()[0], o.coords()[1], o.coords()[2]).getBlock().setType(sign);
		
	}
	
	private void setBlock(double[] pos) {
		Block b = new Location(world, pos[0], pos[1], pos[2]).getBlock();
		if (Func.primeContain(VALID_STONE, b.getType()))
			b.setType(ore);
		else if (b.getType() == Material.DEEPSLATE || b.getType() == Material.TUFF)
			if (ore.toString().endsWith("_ORE"))
				b.setType(Material.valueOf("DEEPSLATE_".concat(ore.toString())));
			else b.setType(ore);
	}
	
	private static boolean addPosition(int[] pos, List<int[]> array) {
		for (int[] e : array)
			if (e[0] == pos[0] && e[1] == pos[1] && e[2] == pos[2])
				return false;
		return array.add(pos);
	}
	
	private static void addFaces(int[] pos, List<int[]> array) {
		addPosition(new int[] {pos[0] + 1, pos[1]    , pos[2]    }, array);
		addPosition(new int[] {pos[0] - 1, pos[1]    , pos[2]    }, array);
		addPosition(new int[] {pos[0]    , pos[1] + 1, pos[2]    }, array);
		addPosition(new int[] {pos[0]    , pos[1] - 1, pos[2]    }, array);
		addPosition(new int[] {pos[0]    , pos[1]    , pos[2] + 1}, array);
		addPosition(new int[] {pos[0]    , pos[1]    , pos[2] - 1}, array);
	}

}