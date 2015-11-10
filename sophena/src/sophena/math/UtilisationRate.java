package sophena.math;

public class UtilisationRate {

	private final double standByLoss;

	private double usageDuration = 8760;
	private double fullLoadHours;
	private double efficiencyRate;

	private UtilisationRate(double standByLoss) {
		this.standByLoss = standByLoss;
	}

	public static UtilisationRate forBigBoiler() {
		UtilisationRate ur = new UtilisationRate(0.0055);
		return ur;
	}

	public static UtilisationRate forSmallBoiler() {
		UtilisationRate ur = new UtilisationRate(0.014);
		return ur;
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
