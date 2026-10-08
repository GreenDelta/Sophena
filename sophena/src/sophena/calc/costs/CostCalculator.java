package sophena.calc.costs;

import sophena.calc.ProjectResult;
import sophena.calc.costs.CostResult.FieldSet;
import sophena.calc.kpi.GeneratedElectricity;
import sophena.model.CostSettings;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;

public class CostCalculator {

	private final ProjectResult result;
	private final Project project;

	private CostSettings settings;
	private boolean withFunding;

	public CostCalculator(ProjectResult result) {
		this.result = result;
		this.project = result.project;
		settings = project.costSettings;
		if (settings == null) {
			settings = new CostSettings();
		}
	}

	public void withFunding(boolean withFunding) {
		this.withFunding = withFunding;
	}

	public CostResult calculate() {
		var r = new CostResult();

		for (var ii : InvestmentItem.allOf(project)) {
			r.items.add(itemOf(r, ii));
		}

		finishCapitalCosts(r);
		addOtherCosts(r);
		addRevenues(r);
		calcTotals(r.dynamicTotal);
		calcTotals(r.staticTotal);
		return r;
	}


	private CostResultItem itemOf(CostResult r, InvestmentItem ii) {

		r.dynamicTotal.investments += ii.initialInvestment();
		r.staticTotal.investments += ii.initialInvestment();

		// add capital costs
		double capitalCosts = capitalCostsOf(ii, settings.investmentFactor);
		r.dynamicTotal.capitalCosts += capitalCosts;
		r.staticTotal.capitalCosts += capitalCostsOf(ii, 1.0);

		// add operation costs = operation + maintenance
		double operationCosts = ii.operation() * settings.hourlyWage;
		double maintenanceCosts = Investments.maintenanceBase(ii);
		double operationRelatedCosts = annuityOf(operationCosts, settings.operationFactor)
			+ annuityOf(maintenanceCosts, settings.maintenanceFactor);
		r.dynamicTotal.operationCosts += operationRelatedCosts;
		r.staticTotal.operationCosts += staticAnnuityOf(operationCosts)
			+ staticAnnuityOf(maintenanceCosts);

		double demandRelatedCosts = ii.producer() != null
			? demandCostsOf(r, ii.producer())
			: 0;

		return new CostResultItem(
			ii, capitalCosts, demandRelatedCosts, operationRelatedCosts);
	}

	/// The annual capital costs of the given item.
	private double capitalCostsOf(InvestmentItem investment, double priceChange) {
		return Investments.capitalCosts(
			investment,
			project.duration,
			interestRate(),
			priceChange);
	}


	private double demandCostsOf(CostResult r, Producer p) {

		var energyResult = result.energyResult;
		double producedHeat = energyResult.totalHeat(p);

		double fuelCosts = FuelCosts.get(result, p);
		double electricityCosts = ElectricityCosts.net(producedHeat, settings);
		double ashCosts = FuelCosts.getAshCosts(result, p);
		double priceChangeFuel = FuelCosts.getPriceChangeFactor(p, settings);

		// we assume the same price change factor for the ash costs as for the fuel
		double demandCosts = annuityOf(fuelCosts, priceChangeFuel)
			+ annuityOf(electricityCosts, settings.electricityFactor)
			+ annuityOf(ashCosts, priceChangeFuel);
		r.dynamicTotal.consumptionCosts += demandCosts;

		r.staticTotal.consumptionCosts += staticAnnuityOf(fuelCosts)
			+ staticAnnuityOf(electricityCosts)
			+ staticAnnuityOf(ashCosts);

		return demandCosts;
	}

	/// Reduce capital costs by fundings and connection fees.
	private void finishCapitalCosts(CostResult r) {
		double bonus = settings.connectionFees;
		if (withFunding) {
			double funding = Funding.get(project, r);
			r.dynamicTotal.funding = funding;
			r.staticTotal.funding = funding;
			bonus += funding;
		}
		if (bonus <= 0)
			return;
		double a = Costs.annuityFactor(project.duration, interestRate());
		r.dynamicTotal.capitalCosts -= (bonus * a);
		r.staticTotal.capitalCosts -= (bonus * a);
	}

	private void addOtherCosts(CostResult r) {

		// the total share of other costs of the investment sum
		double share = (
			settings.insuranceShare
				+ settings.otherShare
				+ settings.administrationShare
		) / 100;

		double staticSum = share * r.staticTotal.investments;
		double dynamicSum = share * r.dynamicTotal.investments;

		for (var annualCosts : settings.annualCosts) {
			staticSum += annualCosts.value;
			dynamicSum += annualCosts.value;
		}

		r.dynamicTotal.otherAnnualCosts = annuityOf(
			dynamicSum, settings.operationFactor);
		r.staticTotal.otherAnnualCosts = staticAnnuityOf(staticSum);
	}

	/// Add revenues from generated electricity and heat.
	private void addRevenues(CostResult r) {
		double electricityRevenues =
			GeneratedElectricity.getTotal(result) * settings.electricityRevenues;

		r.dynamicTotal.revenuesElectricity = annuityOf(
			electricityRevenues, settings.electricityRevenuesFactor);
		r.staticTotal.revenuesElectricity = staticAnnuityOf(electricityRevenues);

		double revenuesHeat = usedHeat() * settings.heatRevenues;
		r.dynamicTotal.revenuesHeat = annuityOf(
			revenuesHeat, settings.heatRevenuesFactor);
		r.staticTotal.revenuesHeat = staticAnnuityOf(revenuesHeat);
	}

	private void calcTotals(FieldSet costs) {
		costs.totalAnnualCosts = costs.capitalCosts
			+ costs.consumptionCosts
			+ costs.operationCosts
			+ costs.otherAnnualCosts;
		costs.annualSurplus = costs.revenuesHeat
			+ costs.revenuesElectricity - costs.totalAnnualCosts;

		double usedHeat = usedHeat();
		costs.heatGenerationCosts = usedHeat != 0
			? (costs.totalAnnualCosts - costs.revenuesElectricity) / usedHeat
			: 0;
	}

	private double staticAnnuityOf(double firstYearValue) {
		return annuityOf(firstYearValue, 1.0);
	}

	private double annuityOf(double firstYearValue, double priceChangeFactor) {
		return Costs.annuity(
			project.duration,
			firstYearValue,
			interestRate(),
			priceChangeFactor
		);
	}

	/**
	 * Returns the interest rate that is used for the calculation.
	 */
	private double interestRate() {
		return withFunding
			? settings.interestRateFunding
			: settings.interestRate;
	}

	/// Returns the used heat in MWh.
	private double usedHeat() {
		var energyResult = result.energyResult;
		double bufferLoss = Stats.sum(energyResult.bufferLoss);
		return (energyResult.totalProducedHeat
			- energyResult.heatNetLoss
			- bufferLoss) / 1000d;
	}

}
