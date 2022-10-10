package fr.loferga.lost_settlers.map.geometry;

import org.bukkit.Location;

public class Vector {
	
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double x;
	public double y;
	public double z;
	
	public Location addTo(Location location) {
		return location.add(x, y, z);
	}
	
	public double[] values() {
		return new double[] {x, y, z};
	}
	
	public double length() {
		return Math.sqrt(x*x + y*y + z*z);
	}
	
	public Vector multiply(double f) {
		x*=f; y*=f; z*=f;
		return this;
	}
	
	public Vector normalize() {
		double len = length();
		x/=len; y/=len; z/=len;
		return this;
	}
	
	public Matrix toMatrix() {
		return new Matrix(new double[][] {{x}, {y}, {z}});
	}
	
	public Vector clone() {
		return new Vector(x, y, z);
	}
	
}