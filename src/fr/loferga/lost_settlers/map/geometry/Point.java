package fr.loferga.lost_settlers.map.geometry;

public class Point {
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
		
	public double x;
	public double y;
	public double z;
	
	public double[] coords() {
		return new double[] {x, y, z};
	}
	
	public Vector vectorTo(double px, double py, double pz) {
		return new Vector(px-x, py-y, pz-z);
	}

}