package sophena.calc;

public final class BoilerEfficiency {

	private BoilerEfficiency() {
	}

	public static double getUtilisationRate(double efficiencyRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double nk = 8760;
		double av = fullLoadHours;
		double bv = 0.014;
		double bw = 1 / ((nk / av - 1) * bv + 1);
		return efficiencyRate * bw;
	}

	public static double getEfficiencyRate(double utilisationRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double nk = 8760;
		double av = fullLoadHours;
		double bv = 0.014;
		return utilisationRate * ((nk / av - 1) * bv + 1);
	}
}
