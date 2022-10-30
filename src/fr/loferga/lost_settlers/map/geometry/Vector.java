package fr.loferga.lost_settlers.map.geometry;

import org.bukkit.Location;

import fr.loferga.lost_settlers.util.Func;

public class Vector {
	
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public double x;
	public double y;
	public double z;
	
	public static Vector random() {
		return new Vector(Func.random(-1, 1), Func.random(-1, 1), Func.random(-1, 1));
	}
	
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
	
	public Vector cross(Vector a) {
		Vector c = clone();
		x = c.y*a.z - c.z*a.y;
		y = c.z*a.x - c.x*a.z;
		z = c.x*a.y - c.y*a.x;
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