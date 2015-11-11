package sophena.math.energetic;

public class WoodAmount {

	private final double woodMass;

	private double factor;
	private double waterContent;
	private double density;

	private WoodAmount(double woodMass) {
		this.woodMass = woodMass;
	}

	public static WoodAmount ofMass_kg(double woodMass) {
		return new WoodAmount(woodMass);
	}

	public WoodAmount waterContent(double waterContent) {
		this.waterContent = waterContent;
		return this;
	}

	public WoodAmount woodDensity_kg_per_m3(double density) {
		this.density = density;
		return this;
	}

	public double getAmountInLogs_stere() {
		this.factor = 0.7;
		return get();
	}

	public double getAmountInChips_m3() {
		this.factor = 0.4;
		return get();
	}

	private double get() {
		return woodMass * (1 - waterContent) / (factor * density);
	}

}
