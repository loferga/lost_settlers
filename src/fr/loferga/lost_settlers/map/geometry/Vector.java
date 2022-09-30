package fr.loferga.lost_settlers.map.geometry;

import org.bukkit.Location;

public class Vector {
	
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	private double x;
	private double y;
	private double z;

	public double x() {
		return x;
	}
	
	public double y() {
		return y;
	}
	
	public double z() {
		return z;
	}
	
	public void setX(double x) {
		this.x = x;
	}
	
	public void setY(double y) {
		this.y = y;
	}
	
	public void setZ(double z) {
		this.z = z;
	}
	
	public void addTo(Location location) {
		location.add(x, y, z);
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
	
}