package sophena.model;

public enum WoodType {

	NONE("-", 1.0),

	CHIPS("Srm", 0.4),

	LOGS("Ster", 0.7);

	// typical unit for the wood type
	private final String unit;

	// conversion factor for converting the wood type specific unit to solid
	// cubic meters
	private final double factor;

	private WoodType(String unit, double factor){
		this.unit = unit;
		this.factor = factor;
	}

	public String getUnit() {
		return unit;
	}

	public double getFactor() {
		return factor;
	}
}
