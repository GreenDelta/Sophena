package sophena.math.energetic;

public class WoodMass {

	private WoodMass() {
	}

	public static FromAmount ofWoodLogs_stere(double amount) {
		return new FromAmount(0.7, amount);
	}

	public static FromAmount ofWoodChips_m3(double amount) {
		return new FromAmount(0.4, amount);
	}

	public static FromEnergy ofEnergy_kWh(double energy) {
		return new FromEnergy(energy);
	}

	public static class FromAmount {

		private final double factor;
		private final double amount;

		private double waterContent;
		private double density;

		private FromAmount(double factor, double amount) {
			this.factor = factor;
			this.amount = amount;
		}

		public FromAmount waterContent(double waterContent) {
			this.waterContent = waterContent;
			return this;
		}

		public FromAmount woodDensity_kg_per_m3(double density) {
			this.density = density;
			return this;
		}

		public double get_t() {
			return (amount * factor * density / 1000) / (1 - waterContent);
		}
	}

	public static class FromEnergy {

		private final double energy;

		private double calorificValue;
		private double waterContent;

		private FromEnergy(double energy) {
			this.energy = energy;
		}

		public FromEnergy calorificValue_kWh_per_kg(double calorificValue) {
			this.calorificValue = calorificValue;
			return this;
		}

		public FromEnergy waterContent(double waterContent) {
			this.waterContent = waterContent;
			return this;
		}

		public double get_kg() {
			return energy / ((1 - waterContent) * calorificValue - waterContent * 0.68);
		}
	}
}
