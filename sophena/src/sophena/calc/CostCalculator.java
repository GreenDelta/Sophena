package sophena.calc;

import sophena.calc.costs.ElectricityCosts;
import sophena.calc.costs.FuelCosts;
import sophena.model.CostSettings;
import sophena.model.Producer;
import sophena.model.ProductCosts;
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
		settings = project.costSettings;
		if (settings == null)
			settings = new CostSettings();
		projectDuration = project.projectDuration;
	}

	public void withFunding(boolean withFunding) {
		this.withFunding = withFunding;
	}

	public CostResult calculate() {
		CostResult r = new CostResult();

		Costs.each(project, costs -> {
			r.netTotal.investments += costs.investment;
			r.grossTotal.investments += vat() * costs.investment;
			addCapitalCosts(r, costs);
			addOperationCosts(r, costs);
		});
		for (Producer p : project.producers)
			addDemandCosts(r, p);
		if (withFunding)
			setCapitalCostsFunding(r);
		addOtherCosts(r);
		r.netTotal.revenues = settings.electricityRevenues;
		r.grossTotal.revenues = settings.electricityRevenues * vat();
		calcTotals(r);
		return r;
	}

	private void addCapitalCosts(CostResult r, ProductCosts costs) {
		double capNetto = getCapitalCosts(costs.duration,
				costs.investment);
		r.netTotal.capitalCosts += capNetto;
		r.grossTotal.capitalCosts += vat() * capNetto;
	}

	private void addDemandCosts(CostResult r, Producer p) {
		double priceChangeFactor = FuelCosts.getPriceChangeFactor(p, settings);
		double cashValueFactor = getCashValueFactor(priceChangeFactor);
		double producedHeat = energyResult.totalHeat(p);
		double netCosts = FuelCosts.net(p, producedHeat)
				+ ElectricityCosts.net(producedHeat, settings);
		r.netTotal.consumptionCosts += netCosts * cashValueFactor * getAnnuityFactor();
		double grossCosts = FuelCosts.gross(p, producedHeat)
				+ ElectricityCosts.gross(producedHeat, settings);
		r.grossTotal.consumptionCosts += grossCosts * cashValueFactor * getAnnuityFactor();
	}

	private void addOperationCosts(CostResult r, ProductCosts costs) {
		double af = getAnnuityFactor();
		double opFactor = getCashValueFactor(settings.operationFactor);
		double maFactor = getCashValueFactor(settings.maintenanceFactor);
		double opNetto = costs.operation * settings.hourlyWage * af * opFactor
				+ costs.investment
						* (costs.repair / 100 + costs.maintenance / 100) * af
						* maFactor;
		r.netTotal.operationCosts += opNetto;
		r.grossTotal.operationCosts += vat() * opNetto;
	}

	private void setCapitalCostsFunding(CostResult r) {
		double ir = ir();
		r.netTotal.capitalCosts = r.netTotal.capitalCosts - settings.funding
				* (Math.pow(ir, projectDuration) * (ir - 1)
						/ (Math.pow(ir, projectDuration) - 1));
		r.grossTotal.capitalCosts += vat() * r.netTotal.capitalCosts;
	}

	private void addOtherCosts(CostResult r) {
		double annuityFactor = getAnnuityFactor();
		double cashValueFactor = getCashValueFactor(settings.operationFactor);
		double share = (settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare) / 100;
		r.netTotal.otherCosts = share * r.netTotal.investments * annuityFactor
				* cashValueFactor;
		r.grossTotal.otherCosts = r.netTotal.otherCosts * vat();
	}

	double getCashValueFactor(double priceChangeFactor) {
		double ir = ir();
		if (ir == priceChangeFactor)
			return ((double) projectDuration) / ir;
		else
			return (1 - Math.pow(priceChangeFactor / ir, projectDuration))
					/ (ir - priceChangeFactor);
	}

	double getCashValueOfReplacement(int replacement, int usageDuration,
			double investmentCosts) {
		if (usageDuration <= 0)
			return 0;
		double ir = ir();
		double priceChange = settings.investmentFactor;
		double year = replacement * usageDuration;
		return investmentCosts * Math.pow(priceChange, year)
				/ Math.pow(ir, year);
	}

	double getResidualValue(int usageDuration, double investmentCosts) {
		if (usageDuration < 1)
			return 0;
		double ir = ir();
		double priceChange = settings.investmentFactor;
		int replacements = getNumberOfReplacements(usageDuration);
		return investmentCosts
				* Math.pow(priceChange, replacements * usageDuration)
				* (((replacements + 1) * usageDuration - projectDuration)
						/ usageDuration)
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

	private void calcTotals(CostResult r) {
		r.netTotal.annualCosts = r.netTotal.capitalCosts
				+ r.netTotal.consumptionCosts
				+ r.netTotal.operationCosts
				+ r.netTotal.otherCosts
				- r.netTotal.revenues;
		r.grossTotal.annualCosts = vat() * r.netTotal.annualCosts;
		r.netTotal.heatGenerationCosts = r.netTotal.annualCosts
				/ energyResult.totalProducedHeat;
		r.grossTotal.heatGenerationCosts = vat() * r.netTotal.heatGenerationCosts;
	}

	/** Returns the interest rate that is used for the calculation. */
	private double ir() {
		double rawRate = withFunding ? settings.interestRateFunding
				: settings.interestRate;
		return 1 + rawRate / 100;
	}

	/** Returns the VAT rate that is used for the calculation. */
	private double vat() {
		return 1 + settings.vatRate / 100;
	}

}
