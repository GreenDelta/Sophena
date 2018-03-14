package sophena.model;

/**
 * Wood amounts can be given in the different quantity types: (dry) mass, chips,
 * logs.
 */
public enum WoodAmountType {

	MASS("t atro", 1),

	CHIPS("Srm", 0.4),

	LOGS("Ster (Rm)", 0.7);

	// typical unit for the wood type
	private final String unit;

	// conversion factor for converting the wood type specific unit to solid
	// cubic meters
	private final double factor;

	private WoodAmountType(String unit, double factor) {
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
