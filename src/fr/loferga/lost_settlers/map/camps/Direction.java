package fr.loferga.lost_settlers.map.camps;

import fr.loferga.lost_settlers.map.geometry.Vector;

public enum Direction {
	
	NORTH(0, 0, -1), EAST(1, 0, 0), SOUTH(0, 0, 1), WEST(-1, 0, 0);
	
	public Vector vector;
	
	Direction(double x, double y, double z) {
		this.vector = new Vector(x, y, z);
	}
	
}