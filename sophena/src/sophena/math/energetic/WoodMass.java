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

}
