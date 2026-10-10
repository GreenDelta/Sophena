package sophena.calc.biogas.costs;

import java.util.List;

import sophena.calc.biogas.BiogasRuntimeResult;
import sophena.calc.costs.Annuity;
import sophena.calc.costs.InvestmentItem;
import sophena.calc.costs.Investments;
import sophena.model.AnnualCostEntry;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.SubstrateProfile;

/// Calculator for the economic evaluation of a biogas plant.
///
/// It calculates the annual costs (capital, consumption, operation, other) and
/// the revenues from electricity sales based on the VDI 2067 annuity method.
/// The dynamic values are calculated with the price change factors of the
/// plant settings; the static values use a price change factor of 1.
public class BiogasCostCalculator {

	private final BiogasPlant plant;
	private final BiogasRuntimeResult result;
	private final BiogasRuntimeRevenues revenues;

	/// Creates a new calculator for the given plant with its runtime result and
	/// the revenues from the electricity feed-in. The revenues must be
	/// calculated before the cost result is created.
	public BiogasCostCalculator(
		BiogasPlant plant,
		BiogasRuntimeResult result,
		BiogasRuntimeRevenues revenues
	) {
		this.plant = plant;
		this.result = result;
		this.revenues = revenues;
	}

	/// Calculates the economic performance of the biogas plant.
	public BiogasCostResult calculate() {
		var r = new BiogasCostResult();
		if (plant == null)
			return r;

		var items = InvestmentItem.allOf(plant);
		for (var item : items) {
			addItem(r, costsOf(item));
		}

		finishCapitalCosts(r);
		addConsumptionCosts(r);
		addOtherCosts(r, items);
		addRevenues(r);
		addAnnualFunding(r);

		calcTotals(r.dynamicTotal);
		calcTotals(r.staticTotal);
		addSuggestedHeatPrices(r);
		return r;
	}

	/// The computed costs of a single investment item: the dynamic values for
	/// the item and the static values for the static totals.
	private ItemCosts costsOf(InvestmentItem item) {

		double capitalCosts = capitalCostsOf(
			item, plant.settings.investmentFactor);
		double staticCapitalCosts = capitalCostsOf(item, 1.0);

		double operationBase = Investments.operationBaseOf(
			item, plant.settings.hourlyWage);
		double maintenanceBase = Investments.maintenanceBaseOf(item);
		double operationCosts = dynamicYearly(
			operationBase, plant.settings.operationFactor)
			+ dynamicYearly(maintenanceBase, plant.settings.maintenanceFactor);
		double staticOperationCosts = staticYearly(operationBase)
			+ staticYearly(maintenanceBase);

		return new ItemCosts(
			item, capitalCosts, operationCosts,
			staticCapitalCosts, staticOperationCosts);
	}

	/// Adds the computed costs of a single item to the result.
	private void addItem(BiogasCostResult r, ItemCosts costs) {

		double investment = costs.item().initialInvestment();
		r.dynamicTotal.investments += investment;
		r.staticTotal.investments += investment;

		r.dynamicTotal.capitalCosts += costs.capitalCosts();
		r.staticTotal.capitalCosts += costs.staticCapitalCosts();

		r.dynamicTotal.operationCosts += costs.operationCosts();
		r.staticTotal.operationCosts += costs.staticOperationCosts();
	}

	/// The annual capital costs of the given item.
	private double capitalCostsOf(InvestmentItem item, double priceChange) {
		return Investments.capitalCosts(
			item,
			plant.duration,
			plant.settings.interestRate,
			priceChange);
	}

	/// Reduces the capital costs by the investment funding.
	private void finishCapitalCosts(BiogasCostResult r) {
		double funding = plant.settings.funding;
		if (funding <= 0)
			return;
		r.dynamicTotal.investmentFunding = funding;
		r.staticTotal.investmentFunding = funding;
		double a = Annuity.factor(
			plant.duration, plant.settings.interestRate);
		r.dynamicTotal.capitalCosts -= (funding * a);
		r.staticTotal.capitalCosts -= (funding * a);
	}

	/// Adds the costs for the substrates and the electricity that is purchased
	/// from the grid.
	private void addConsumptionCosts(BiogasCostResult r) {

		double substrateCosts = 0;
		for (SubstrateProfile profile : plant.substrateProfiles) {
			substrateCosts += profile.annualMass * profile.substrateCosts;
		}

		double electricityCosts = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			// grid power is needed in full feed-in mode or when the plant is idle
			if (plant.settings.isFullFeedIn || !result.runFlags()[h]) {
				electricityCosts += plant.settings.avgPowerDemand
					* plant.settings.electricityPrice;
			}
		}

		r.dynamicTotal.consumptionCosts = dynamicYearly(
			substrateCosts, plant.settings.bioFuelFactor)
			+ dynamicYearly(electricityCosts, plant.settings.electricityFactor);
		r.staticTotal.consumptionCosts = staticYearly(substrateCosts)
			+ staticYearly(electricityCosts);
	}

	/// Adds the insurance and the other fixed annual costs.
	private void addOtherCosts(BiogasCostResult r, List<InvestmentItem> items) {

		// the insurance is a share of the full investment value
		double insurance = totalInvestmentValue(items)
			* (plant.settings.insuranceCostsShare / 100);

		double entries = 0;
		if (plant.otherAnnualCosts != null) {
			for (AnnualCostEntry entry : plant.otherAnnualCosts) {
				entries += entry.value;
			}
		}

		double otherCosts = insurance + entries;
		r.dynamicTotal.otherAnnualCosts = dynamicYearly(
			otherCosts, plant.settings.operationFactor);
		r.staticTotal.otherAnnualCosts = staticYearly(otherCosts);
	}

	/// Adds the revenues from the electricity feed-in. These are the total
	/// revenues of the plant: the funding and the exchange revenues minus the
	/// share of the direct marketer (see `BiogasRuntimeRevenues`).
	private void addRevenues(BiogasCostResult r) {
		double amount = revenues.totalRevenues();
		r.dynamicTotal.revenuesElectricity = dynamicYearly(
			amount, plant.settings.electricityRevenuesFactor);
		r.staticTotal.revenuesElectricity = staticYearly(amount);
	}

	private void addAnnualFunding(BiogasCostResult r) {
		double annualFunding = plant.settings.annualFunding;
		if (annualFunding <= 0)
			return;
		// The annual funding is typically a constant value over the
		// complete project time; so no price change is included here;
		// also it is not something that reduces the capital costs
		// but works more like revenues in the results
		r.dynamicTotal.annualFunding = staticYearly(annualFunding);
		r.staticTotal.annualFunding = staticYearly(annualFunding);
	}

	/// The total value of the plant investments in EUR. In contrast to the
	/// initial investment this ignores the refurbishment shares, e.g. for the
	/// calculation of the insurance costs.
	private static double totalInvestmentValue(List<InvestmentItem> items) {
		double sum = 0;
		for (var item : items) {
			sum += item.investment();
		}
		return sum;
	}

	private double staticYearly(double firstYearCosts) {
		return dynamicYearly(firstYearCosts, 1.0);
	}

	private double dynamicYearly(double firstYearCosts, double priceChange) {
		return Annuity.ofYearlyCosts(
			plant.duration,
			firstYearCosts,
			plant.settings.interestRate,
			priceChange);
	}

	private static void calcTotals(BiogasCostResult.Summary costs) {
		costs.totalAnnualCosts = costs.capitalCosts
			+ costs.consumptionCosts
			+ costs.operationCosts
			+ costs.otherAnnualCosts;
		costs.annualSurplus = costs.revenuesElectricity
			+ costs.annualFunding
			- costs.totalAnnualCosts;
	}

	/// Sets the suggested heat price of the dynamic and the static summary: the
	/// price in EUR/MWh where the annual surplus reaches the expected profit of
	/// the plant settings (the total capital return rate or the expected annual
	/// surplus).
	private void addSuggestedHeatPrices(BiogasCostResult r) {
		double heat = revenues != null
			? revenues.totalGeneratedHeat()
			: 0;
		r.dynamicTotal.suggestedHeatPrice =
			suggestedHeatPrice(r.dynamicTotal, heat);
		r.staticTotal.suggestedHeatPrice =
			suggestedHeatPrice(r.staticTotal, heat);
	}

	/// The suggested heat price in EUR/MWh of the given summary. With
	/// `WP = 1000 * WE / WM` and `WE = target - annualSurplus`, where the target
	/// is `JGK * GKR / 100` in the capital return rate case and `JUB` in the
	/// annual surplus case.
	private double suggestedHeatPrice(
		BiogasCostResult.Summary summary, double heat
	) {
		if (heat <= 0)
			return 0;
		var settings = plant.settings;
		double target = settings.useCapitalReturnRate
			? summary.totalAnnualCosts * settings.capitalReturnRate / 100.0
			: settings.expectedAnnualSurplus;
		double heatRevenue = target - summary.annualSurplus;
		return 1000 * heatRevenue / heat;
	}

	/// The computed costs of a single investment item: the dynamic values for
	/// the item and the static values for the static totals.
	private record ItemCosts(
		InvestmentItem item,
		double capitalCosts,
		double operationCosts,
		double staticCapitalCosts,
		double staticOperationCosts
	) {
	}
}
