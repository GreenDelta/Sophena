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
			r.netto.investments += costs.investment;
			r.brutto.investments += settings.vatRate * costs.investment;
			addCapitalCosts(r, costs);
			addOperationCosts(r, costs);
		}
		addCapitalCostsFunding(r);
		addOtherCosts(r);
		r.netto.revenues = settings.electricityRevenues;
		r.brutto.revenues = settings.electricityRevenues * settings.vatRate;
		return r;
	}

	private void addCapitalCosts(CostResult r, ComponentCosts costs) {
		double capNetto = getCapitalCosts(costs.duration,
				costs.investment);
		r.netto.capitalCosts += capNetto;
		r.brutto.capitalCosts += settings.vatRate * capNetto;
	}

	private void addOperationCosts(CostResult r, ComponentCosts costs) {
		double af = getAnnuityFactor();
		double opFactor = getCashValueFactor(settings.operationFactor);
		double maFactor = getCashValueFactor(settings.maintenanceFactor);
		double opNetto =
				costs.operation * settings.hourlyWage * af * opFactor
						+ costs.investment
						* (costs.repair / 100 + costs.maintenance / 100) * af
						* maFactor;
		r.netto.operationCosts += opNetto;
		r.brutto.operationCosts += settings.vatRate * opNetto;
	}

	private void addCapitalCostsFunding(CostResult r) {
		r.netto.capitalCostsFunding = r.netto.capitalCosts
				- settings.funding
				* (Math.pow(settings.interestRateFunding, projectDuration)
						* (settings.interestRateFunding - 1)
						/ (Math.pow(settings.interestRateFunding,
						projectDuration) - 1));
		r.brutto.capitalCostsFunding += settings.vatRate
				* r.netto.capitalCostsFunding;
	}

	private void addOtherCosts(CostResult r) {
		double annuityFactor = getAnnuityFactor();
		double cashValueFactor = getCashValueFactor(settings.operationFactor);
		double share = (settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare) / 100;
		r.netto.otherCosts = share * r.netto.investments * annuityFactor
				* cashValueFactor;
		r.brutto.otherCosts = r.netto.otherCosts * settings.vatRate;
	}

	double getAnnuityFactor() {
		double ir = settings.interestRate;
		return (ir - 1) / (1 - Math.pow(ir, -projectDuration));
	}

	double getCashValueFactor(double priceChangeFactor) {
		double ir = settings.interestRate;
		if (ir == priceChangeFactor)
			return ((double) projectDuration) / ir;
		else
			return (1 - Math.pow(priceChangeFactor / ir, projectDuration))
					/ (ir - priceChangeFactor);
	}

	int getNumberOfReplacements(int usageDuration) {
		if (usageDuration >= projectDuration || usageDuration == 0)
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
		double ir = settings.interestRate;
		double priceChange = settings.investmentFactor;
		double year = replacement * usageDuration;
		return investmentCosts * Math.pow(priceChange, year)
				/ Math.pow(ir, year);
	}

	double getResidualValue(int usageDuration, double investmentCosts) {
		double ir = settings.interestRate;
		double priceChange = settings.investmentFactor;
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

}
