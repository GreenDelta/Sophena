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
		return interestRate / (1 - Math.pow(1 + interestRate, -duration));
	}

	public static double get(Project project) {
		if (project == null || project.costSettings == null)
			return 0;
		double ir = project.costSettings.interestRate / 100;
		return AnnuitiyFactor
				.ofInterestRate(ir)
				.withDuration_years(project.projectDuration)
				.get();
	}

	public static double getForFunding(Project project) {
		if (project == null || project.costSettings == null)
			return 0;
		double ir = project.costSettings.interestRateFunding / 100;
		return AnnuitiyFactor
				.ofInterestRate(ir)
				.withDuration_years(project.projectDuration)
				.get();
	}

}
