package fr.loferga.lost_settlers.map.camps;

import fr.loferga.lost_settlers.map.geometry.Vector;

public enum Direction {
	
	NORTH(1, 0, 0), EAST(0, 0, 1), SOUTH(-1, 0, 0), WEST(0, 0, -1);
	
	protected Vector vector;
	
	Direction(double x, double y, double z) {
		this.vector = new Vector(x, y, z);
	}
	
}