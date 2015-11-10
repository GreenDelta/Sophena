package sophena.math;

public class EfficiencyRate {

	private final double standByLoss;

	private double usageDuration = 8760;
	private double fullLoadHours;
	private double utilisationRate;

	private EfficiencyRate(double standByLoss) {
		this.standByLoss = standByLoss;
	}

	public static EfficiencyRate forBigBoiler() {
		return new EfficiencyRate(0.0055);
	}

	public static EfficiencyRate forSmallBoiler() {
		return new EfficiencyRate(0.014);
	}

	public EfficiencyRate usageDuration_h(double usageDuration) {
		this.usageDuration = usageDuration;
		return this;
	}

	public EfficiencyRate fullLoadHours_h(double fullLoadHours) {
		this.fullLoadHours = fullLoadHours;
		return this;
	}

	public EfficiencyRate utilisationRate(double utilisationRate) {
		this.utilisationRate = utilisationRate;
		return this;
	}

	public double get() {
		if (fullLoadHours == 0)
			return 0;
		return utilisationRate * ((usageDuration / fullLoadHours - 1) * standByLoss + 1);
	}
}
