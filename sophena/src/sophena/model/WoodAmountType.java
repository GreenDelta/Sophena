package sophena.model;

public enum WoodAmountType {

	MASS("kg", 1),

	CHIPS("Srm", 0.4),

	LOGS("Ster", 0.7);

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
