package sophena.calc;

import sophena.model.ComponentCosts;
import sophena.model.CostSettings;
import sophena.model.Producer;
import sophena.model.Project;

class CostCalculator {

	private Project project;
	private EnergyResult energyResult;

	private CostSettings settings;
	private double projectDuration;
	private boolean withFunding;

	public CostCalculator(Project project, EnergyResult energyResult) {
		this.project = project;
		this.energyResult = energyResult;
		if (project == null)
			project = new Project();
		settings = project.getCostSettings();
		if (settings == null)
			settings = new CostSettings();
		projectDuration = project.getProjectDuration();
	}

	public CostResult calculate() {
		CostResult r = new CostResult();
		// TODO: iterate over all cost components; not only producers
		for (Producer p : project.getProducers()) {
			ComponentCosts costs = p.getCosts();
			if (costs == null)
				continue;
			setWithFunding(false);
			r.netto.investments += costs.investment;
		}

		return r;
	}

	public void setWithFunding(boolean withFunding) {
		this.withFunding = withFunding;
	}

	double getAnnuityFactor() {
		double ir = getInterestRate();
		return (ir - 1) / (1 - Math.pow(ir, -projectDuration));
	}

	double getCashValueFactor(double priceChangeFactor) {
		double ir = getInterestRate();
		if (ir == priceChangeFactor)
			return ((double) projectDuration) / ir;
		else
			return (1 - Math.pow(priceChangeFactor / ir, projectDuration))
					/ (ir - priceChangeFactor);
	}

	int getNumberOfReplacements(int usageDuration) {
		if (usageDuration >= projectDuration)
			return 0;
		double pdur = (double) projectDuration;
		double udur = (double) usageDuration;
		double res = Math.ceil(pdur / udur) - 1.0;
		return (int) res;
	}

	double getCashValueOfReplacement(int replacement, int usageDuration,
			double investmentCosts) {
		if (usageDuration <= 0)
			return 0;
		double ir = getInterestRate();
		double priceChange = settings.getInvestmentFactor();
		double year = replacement * usageDuration;
		return investmentCosts * Math.pow(priceChange, year)
				/ Math.pow(ir, year);
	}

	double getResidualValue(int usageDuration, double investmentCosts) {
		double ir = getInterestRate();
		double priceChange = settings.getInvestmentFactor();
		int replacements = getNumberOfReplacements(usageDuration);
		return investmentCosts
				* Math.pow(priceChange, replacements * usageDuration)
				* (((replacements + 1) * usageDuration - projectDuration) / usageDuration)
				* (1 / Math.pow(ir, projectDuration));
	}

	double getCapitalCosts(int usageDuration, double investmentCosts) {
		double annuityFactor = getAnnuityFactor();
		double residualValue = getResidualValue(usageDuration, investmentCosts);
		if (projectDuration <= usageDuration)
			return (investmentCosts - residualValue) * annuityFactor;
		int replacements = getNumberOfReplacements(usageDuration);
		double costs = investmentCosts;
		for (int i = 1; i <= replacements; i++) {
			costs += getCashValueOfReplacement(i, usageDuration,
					investmentCosts);
		}
		costs -= residualValue;
		return costs * annuityFactor;
	}

	private double getInterestRate() {
		if (withFunding)
			return settings.getInterestRateFunding();
		else
			return settings.getInterestRate();
	}

}
