package fr.loferga.lost_settlers.map;

import org.bukkit.Material;

public class LodeGenerator {
	
	protected LodeGenerator(Material ore, double yratio, int gaussFactor, double gaussOffset, double[] sizeBounds, int count) {
		this.ore = ore;
		this.yratio = yratio;
		this.gaussFactor = gaussFactor;
		this.gaussOffset = gaussOffset;
		this.sizeBounds = sizeBounds;
		this.count = count;
	}
	
	protected Material ore;
	protected double yratio;
	protected int gaussFactor;
	protected double gaussOffset;
	protected double[] sizeBounds;
	protected int count;
	
}