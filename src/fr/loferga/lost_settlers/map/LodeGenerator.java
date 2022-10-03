package fr.loferga.lost_settlers.map;

import org.bukkit.Material;

public class LodeGenerator {
	
	// DATA HOLDER
	
	public LodeGenerator(Material ore, double yratio, int gaussFactor, double gaussOffset, double[] sizeBounds, int count) {
		this.ore = ore;
		this.yratio = yratio;
		this.gaussFactor = gaussFactor;
		this.gaussOffset = gaussOffset;
		this.sizeBounds = sizeBounds;
		this.count = count;
	}
	
	public Material ore;
	public double yratio;
	public int gaussFactor;
	public double gaussOffset;
	public double[] sizeBounds;
	public int count;
	
}