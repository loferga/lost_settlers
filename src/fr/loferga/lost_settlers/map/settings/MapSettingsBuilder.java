package fr.loferga.lost_settlers.map.settings;

import fr.loferga.lost_settlers.map.LodeGenerator;
import fr.loferga.lost_settlers.map.camps.Camp;

public class MapSettingsBuilder {

	// BUILDER DESIGN PATERN FOR MAP SETTINGS DATA RETRIEVER

	// game launch required parameters
	private Integer playableArea = null;
	private Integer campSize = null;
	private Integer vitalSize = null;
	private Camp[] camps = null;

	// game optional parameters
	private Double chamberHeight = null;
	private Integer highestGround = null;
	private LodeGenerator[] generators = null;
	
	public MapSettingsBuilder withPlayableArea(int playableArea) {
		this.playableArea = playableArea;
		return this;
	}
	
	public MapSettingsBuilder withCampSize(int campSize) {
		this.campSize = campSize;
		return this;
	}

	public MapSettingsBuilder withVitalSize(int vitalSize) {
		this.vitalSize = vitalSize;
		return this;
	}

	public MapSettingsBuilder withCamps(Camp[] camps) {
		this.camps = camps;
		return this;
	}

	public MapSettingsBuilder withChamberHeight(Double chamberHeight) {
		this.chamberHeight = chamberHeight;
		return this;
	}

	public MapSettingsBuilder withHighestGround(int highestGround) {
		this.highestGround = highestGround;
		return this;
	}

	public MapSettingsBuilder withGenerators(LodeGenerator[] generators) {
		this.generators = generators;
		return this;
	}

	public MapSettings build() {
		return new MapSettings(playableArea, campSize, vitalSize, camps, chamberHeight, highestGround, generators);
	}

}