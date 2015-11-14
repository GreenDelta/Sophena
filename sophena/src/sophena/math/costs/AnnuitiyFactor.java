package sophena.math.costs;

import sophena.model.Project;

public class AnnuitiyFactor {

	private final double interestRate;
	private double duration;

	private AnnuitiyFactor(double interestRate) {
		this.interestRate = interestRate;
	}

	public static AnnuitiyFactor ofInterestRate(double interestRate) {
		return new AnnuitiyFactor(interestRate);
	}

	public AnnuitiyFactor withDuration_years(double duration) {
		this.duration = duration;
		return this;
	}

	public double get() {
		double i = interestRate / 100;
		double n = duration;
		return i / (1 - Math.pow(1 + i, -n));
	}

	public static double get(Project project, double interestRate) {
		if (project == null)
			return 0;
		double n = project.projectDuration;
		double i = interestRate / 100;
		return i / (1 - Math.pow(1 + i, -n));
	}

}
