package sophena.math.energetic;

class WoodFuelEnergy {

	private final double woodMass;
	private double waterContent;
	private double calorificValue;

	private WoodFuelEnergy(double woodMass) {
		this.woodMass = woodMass;
	}

	public static WoodFuelEnergy ofWoodMass_kg(double woodMass) {
		return new WoodFuelEnergy(woodMass);
	}

	public WoodFuelEnergy waterContent(double waterContent) {
		this.waterContent = waterContent;
		return this;
	}

	public WoodFuelEnergy calorificValue_kWh_per_kg(double calorificValue) {
		this.calorificValue = calorificValue;
		return this;
	}

	public double get_kWh() {
		return woodMass * ((1 - waterContent) * calorificValue - waterContent * 0.68);
	}

}
