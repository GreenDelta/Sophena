package sophena.calc;

import sophena.model.Boiler;
import sophena.model.ComponentCosts;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelSpec;
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

	public void withFunding(boolean withFunding) {
		this.withFunding = withFunding;
	}

	public CostResult calculate() {
		CostResult r = new CostResult();
		// TODO: iterate over all cost components; not only producers
		for (Producer p : project.getProducers()) {
			ComponentCosts costs = p.getCosts();
			if (costs == null)
				continue;
			r.netto.investments += costs.investment;
			r.brutto.investments += vat() * costs.investment;
			addCapitalCosts(r, costs);
			addConsumptionCosts(r, p);
			addOperationCosts(r, costs);
		}
		if (withFunding)
			setCapitalCostsFunding(r);
		addOtherCosts(r);
		r.netto.revenues = settings.electricityRevenues;
		r.brutto.revenues = settings.electricityRevenues * vat();
		calcTotals(r);
		return r;
	}

	private void addCapitalCosts(CostResult r, ComponentCosts costs) {
		double capNetto = getCapitalCosts(costs.duration,
				costs.investment);
		r.netto.capitalCosts += capNetto;
		r.brutto.capitalCosts += vat() * capNetto;
	}

	private void addConsumptionCosts(CostResult r, Producer p) {
		// TODO: electricity usage currently not included
		double fuelCosts = getFuelCosts(p);
		if (fuelCosts == 0 || p.getFuelSpec() == null)
			return;
		double priceChangeFactor = 0;
		if (p.getBoiler().getFuel() != null)
			priceChangeFactor = settings.fossilFuelFactor;
		else
			priceChangeFactor = settings.bioFuelFactor; // wood fuel
		double cashValueFactor = getCashValueFactor(priceChangeFactor);
		double costs = fuelCosts * cashValueFactor * getAnnuityFactor();
		r.netto.consumptionCosts += costs;
		double vat = 1 + p.getFuelSpec().getTaxRate() / 100;
		r.brutto.consumptionCosts += vat * costs;
	}

	private void addOperationCosts(CostResult r, ComponentCosts costs) {
		double af = getAnnuityFactor();
		double opFactor = getCashValueFactor(settings.operationFactor);
		double maFactor = getCashValueFactor(settings.maintenanceFactor);
		double opNetto = costs.operation * settings.hourlyWage * af * opFactor
				+ costs.investment
						* (costs.repair / 100 + costs.maintenance / 100) * af
						* maFactor;
		r.netto.operationCosts += opNetto;
		r.brutto.operationCosts += vat() * opNetto;
	}

	private void setCapitalCostsFunding(CostResult r) {
		double ir = ir();
		r.netto.capitalCosts = r.netto.capitalCosts - settings.funding
				* (Math.pow(ir, projectDuration) * (ir - 1)
						/ (Math.pow(ir, projectDuration) - 1));
		r.brutto.capitalCosts += vat() * r.netto.capitalCosts;
	}

	private void addOtherCosts(CostResult r) {
		double annuityFactor = getAnnuityFactor();
		double cashValueFactor = getCashValueFactor(settings.operationFactor);
		double share = (settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare) / 100;
		r.netto.otherCosts = share * r.netto.investments * annuityFactor
				* cashValueFactor;
		r.brutto.otherCosts = r.netto.otherCosts * vat();
	}

	double getAnnuityFactor() {
		double ir = ir();
		return (ir - 1) / (1 - Math.pow(ir, -projectDuration));
	}

	double getCashValueFactor(double priceChangeFactor) {
		double ir = ir();
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
		double ir = ir();
		double priceChange = settings.investmentFactor;
		double year = replacement * usageDuration;
		return investmentCosts * Math.pow(priceChange, year)
				/ Math.pow(ir, year);
	}

	double getResidualValue(int usageDuration, double investmentCosts) {
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

	private double getFuelCosts(Producer p) {
		double heat = energyResult.totalHeat(p);
		Boiler boiler = p.getBoiler();
		FuelSpec fuelSpec = p.getFuelSpec();
		if (heat == 0 || boiler == null || fuelSpec == null)
			return 0;
		int fullLoadHours = (int) (heat / boiler.getMaxPower());
		double ur = BoilerEfficiency.getUtilisationRateBig(boiler
				.getEfficiencyRate(), fullLoadHours);
		double energyContent = heat / ur;
		Fuel fuel = boiler.getFuel();
		if (fuel != null) {
			// no wood fuel
			double amount = energyContent / fuel.getCalorificValue();
			return amount * fuelSpec.getPricePerUnit();
		}
		// wood fuel
		fuel = fuelSpec.getWoodFuel();
		if (boiler.getWoodAmountType() == null || fuel == null)
			return 0;
		double wc = fuelSpec.getWaterContent() / 100;
		double woodMass = energyContent
				/ ((1 - wc) * fuel.getCalorificValue() - wc * 0.68);
		double woodAmount = woodMass * (1 - wc)
				/ (boiler.getWoodAmountType().getFactor() * fuel.getDensity());
		return woodAmount * fuelSpec.getPricePerUnit();
	}

	private void calcTotals(CostResult r) {
		r.netto.annualCosts = r.netto.capitalCosts
				+ r.netto.consumptionCosts
				+ r.netto.operationCosts
				+ r.netto.otherCosts
				- r.netto.revenues;
		r.brutto.annualCosts = vat() * r.netto.annualCosts;
		r.netto.heatGenerationCosts = r.netto.annualCosts
				/ energyResult.totalProducedHeat;
		r.brutto.heatGenerationCosts = vat() * r.netto.heatGenerationCosts;
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
