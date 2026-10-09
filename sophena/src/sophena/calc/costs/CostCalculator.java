package sophena.calc.costs;

import sophena.calc.ProjectResult;
import sophena.calc.costs.CostResult.Summary;
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
			addItem(r, costsOf(ii));
		}

		finishCapitalCosts(r);
		addOtherCosts(r);
		addRevenues(r);
		calcTotals(r.dynamicTotal);
		calcTotals(r.staticTotal);
		return r;
	}


	private ItemCosts costsOf(InvestmentItem ii) {

		// capital costs
		double capitalCosts = capitalCostsOf(ii, settings.investmentFactor);
		double staticCapitalCosts = capitalCostsOf(ii, 1.0);

		// operation costs = operation + maintenance
		double operationBase = Investments.operationBaseOf(ii, settings.hourlyWage);
		double maintenanceBase = Investments.maintenanceBaseOf(ii);
		double dynamicOperationCosts =
			dynamicYearly(operationBase, settings.operationFactor)
			+ dynamicYearly(maintenanceBase, settings.maintenanceFactor);
		double staticOperationCosts =
			staticYearly(operationBase) + staticYearly(maintenanceBase);

		// demand-related costs
		double dynamicDemandCosts = 0;
		double staticDemandCosts = 0;
		if (ii.producer() != null) {
			var demand = demandCostsOf(ii.producer());
			dynamicDemandCosts = demand.dynamic();
			staticDemandCosts = demand.staticCosts();
		}

		var item = new CostResultItem(
			ii, capitalCosts, dynamicDemandCosts, dynamicOperationCosts);
		return new ItemCosts(
			item, staticCapitalCosts, staticDemandCosts, staticOperationCosts);
	}

	/// Adds the computed costs of a single item to the result.
	private void addItem(CostResult r, ItemCosts costs) {

		var item = costs.item();
		r.items.add(item);

		double investment = item.initialInvestment();
		r.dynamicTotal.investments += investment;
		r.staticTotal.investments += investment;

		r.dynamicTotal.capitalCosts += item.capitalCosts();
		r.staticTotal.capitalCosts += costs.staticCapitalCosts();

		r.dynamicTotal.consumptionCosts += item.demandRelatedCosts();
		r.staticTotal.consumptionCosts += costs.staticDemandCosts();

		r.dynamicTotal.operationCosts += item.operationRelatedCosts();
		r.staticTotal.operationCosts += costs.staticOperationCosts();
	}

	/// The annual capital costs of the given item.
	private double capitalCostsOf(InvestmentItem investment, double priceChange) {
		return Investments.capitalCosts(
			investment,
			project.duration,
			interestRate(),
			priceChange);
	}


	/// The demand-related costs of a producer: the dynamic annuity that is also
	/// shown for the item and the static annuity that is used for the totals.
	private DemandCosts demandCostsOf(Producer p) {

		var energyResult = result.energyResult;
		double producedHeat = energyResult.totalHeat(p);

		double fuelCosts = FuelCosts.get(result, p);
		double electricityCosts = ElectricityCosts.net(producedHeat, settings);
		double ashCosts = FuelCosts.getAshCosts(result, p);
		double priceChangeFuel = FuelCosts.getPriceChangeFactor(p, settings);

		// we assume the same price change factor for the ash costs as for the fuel
		double dynamic = dynamicYearly(fuelCosts, priceChangeFuel)
			+ dynamicYearly(electricityCosts, settings.electricityFactor)
			+ dynamicYearly(ashCosts, priceChangeFuel);

		double staticCosts = staticYearly(fuelCosts)
			+ staticYearly(electricityCosts)
			+ staticYearly(ashCosts);

		return new DemandCosts(dynamic, staticCosts);
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
		double a = Annuity.factor(project.duration, interestRate());
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

		r.dynamicTotal.otherAnnualCosts = dynamicYearly(
			dynamicSum, settings.operationFactor);
		r.staticTotal.otherAnnualCosts = staticYearly(staticSum);
	}

	/// Add revenues from generated electricity and heat.
	private void addRevenues(CostResult r) {
		double electricityRevenues =
			GeneratedElectricity.getTotal(result) * settings.electricityRevenues;

		r.dynamicTotal.revenuesElectricity = dynamicYearly(
			electricityRevenues, settings.electricityRevenuesFactor);
		r.staticTotal.revenuesElectricity = staticYearly(electricityRevenues);

		double heatRevenues = usedHeat() * settings.heatRevenues;
		r.dynamicTotal.revenuesHeat = dynamicYearly(
			heatRevenues, settings.heatRevenuesFactor);
		r.staticTotal.revenuesHeat = staticYearly(heatRevenues);
	}

	private void calcTotals(Summary costs) {
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

	private double staticYearly(double firstYearCosts) {
		return dynamicYearly(firstYearCosts, 1.0);
	}

	private double dynamicYearly(double firstYearCosts, double priceChange) {
		return Annuity.ofYearlyCosts(
			project.duration,
			firstYearCosts,
			interestRate(),
			priceChange
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

	/// The computed costs of a single investment item. The [CostResultItem]
	/// holds the dynamic values that are shown for the item; the static
	/// annuities are only needed for the static totals.
	private record ItemCosts(
		CostResultItem item,
		double staticCapitalCosts,
		double staticDemandCosts,
		double staticOperationCosts
	) {
	}

	/// The demand-related costs of a producer: the dynamic annuity and the
	/// static annuity.
	private record DemandCosts(double dynamic, double staticCosts) {
	}

}
