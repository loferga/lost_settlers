package fr.loferga.lost_settlers.map.geometry;

public class Point {
	
	public Point(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
		
	protected double x;
	protected double y;
	protected double z;
	
	public double x() {
		return x;
	}
	
	public double y() {
		return y;
	}
	
	public double z() {
		return z;
	}
	
	public double[] coords() {
		return new double[] {x, y, z};
	}
		
	public Vector vectorTo(double X, double Y, double Z) {
		return new Vector(X-x, Y-y, Z-z);
	}

}