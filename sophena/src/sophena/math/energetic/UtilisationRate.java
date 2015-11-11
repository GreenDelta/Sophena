package sophena.math.energetic;

/**
 * Calculates the utilisation rate of a boiler.
 */
public class UtilisationRate {

	private final double standByLoss;

	private double usageDuration = 8760;
	private double fullLoadHours;
	private double efficiencyRate;

	private UtilisationRate(double standByLoss) {
		this.standByLoss = standByLoss;
	}

	public static UtilisationRate ofBigBoiler() {
		return new UtilisationRate(0.0055);
	}

	public static UtilisationRate ofSmallBoiler() {
		return new UtilisationRate(0.014);
	}

	public UtilisationRate usageDuration_h(double usageDuration) {
		this.usageDuration = usageDuration;
		return this;
	}

	public UtilisationRate fullLoadHours_h(double fullLoadHours) {
		this.fullLoadHours = fullLoadHours;
		return this;
	}

	public UtilisationRate efficiencyRate(double efficiencyRate) {
		this.efficiencyRate = efficiencyRate;
		return this;
	}

	public double get() {
		if (fullLoadHours == 0)
			return 0;
		double standbyRate = 1 / ((usageDuration / fullLoadHours - 1) * standByLoss + 1);
		return standbyRate * efficiencyRate;
	}

}
