package sophena.calc.biogas.costs;

import java.util.List;

import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.calc.costs.Annuity;
import sophena.calc.costs.CostResultItem;
import sophena.calc.costs.InvestmentItem;
import sophena.calc.costs.Investments;
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

	/// The share of the cooled heat that is needed as electricity for cooling.
	private static final double COOLING_ELECTRICITY_SHARE = 0.005;

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
		addRevenuesHeat(r);
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

		// biogas plants do not calculate demand related costs per item; these
		// are added as aggregated consumption costs, see addConsumptionCosts
		var item = new CostResultItem(
			costs.item(), costs.capitalCosts(), 0, costs.operationCosts());
		r.items.add(item);

		double investment = item.initialInvestment();
		r.dynamicTotal.investments += investment;
		r.staticTotal.investments += investment;

		r.dynamicTotal.capitalCosts += item.capitalCosts();
		r.staticTotal.capitalCosts += costs.staticCapitalCosts();

		r.dynamicTotal.operationCosts += item.operationRelatedCosts();
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

	/// Adds the costs for the substrates, the electricity that is purchased from
	/// the grid, and the electricity that is needed for cooling unused heat.
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

		double coolingCosts = unusedHeat()
			* COOLING_ELECTRICITY_SHARE
			* plant.settings.electricityPrice;

		r.dynamicTotal.consumptionCosts = dynamicYearly(
			substrateCosts, plant.settings.bioFuelFactor)
			+ dynamicYearly(electricityCosts, plant.settings.electricityFactor)
			+ dynamicYearly(coolingCosts, plant.settings.electricityFactor);
		r.staticTotal.consumptionCosts = staticYearly(substrateCosts)
			+ staticYearly(electricityCosts)
			+ staticYearly(coolingCosts);
	}

	/// Adds the insurance, the other charges, and the other fixed annual costs.
	private void addOtherCosts(BiogasCostResult r, List<InvestmentItem> items) {

		// the insurance and the other charges are shares of the full
		// investment value
		double share = (
			plant.settings.insuranceCostsShare + plant.settings.otherCostsShare
		) / 100;
		double costs = totalInvestmentValue(items) * share;

		if (plant.otherAnnualCosts != null) {
			for (var entry : plant.otherAnnualCosts) {
				costs += entry.value;
			}
		}

		r.dynamicTotal.otherAnnualCosts = dynamicYearly(
			costs, plant.settings.operationFactor);
		r.staticTotal.otherAnnualCosts = staticYearly(costs);
	}

	/// Adds the revenues from the electricity feed-in. These are the funding of
	/// the feed-in, the revenues at the monthly market value, and the additional
	/// revenues which are the difference between the exchange revenues and the
	/// reference market revenues. The price change factor of the electricity
	/// revenues is only applied to the additional revenues; the funding and the
	/// reference market revenues are just adjusted with the interest rate. When
	/// a share of the direct marketer is defined, the additional revenues are
	/// reduced by the share of the marketer.
	private void addRevenues(BiogasCostResult r) {
		double funding = revenues != null ? revenues.feedInFundingSum() : 0;
		double reference = revenues != null ? revenues.marketRevenuesSum() : 0;
		double spot = revenues != null ? revenues.spotMarketRevenuesSum() : 0;
		double additional = spot - reference;
		double share = plant.settings.directMarketerShare / 100.0;

		var dyn = r.dynamicTotal;
		dyn.feedInFundingRevenues = staticYearly(funding);
		dyn.referenceMarketRevenues = staticYearly(reference);
		dyn.additionalRevenues = dynamicYearly(
			additional, plant.settings.electricityRevenuesFactor);
		dyn.directMarketerPayment = Math.max(
			0, share * dyn.additionalRevenues);
		dyn.revenuesElectricity = dyn.feedInFundingRevenues
			+ dyn.referenceMarketRevenues
			+ dyn.additionalRevenues
			- dyn.directMarketerPayment;

		var stat = r.staticTotal;
		stat.feedInFundingRevenues = staticYearly(funding);
		stat.referenceMarketRevenues = staticYearly(reference);
		stat.additionalRevenues = staticYearly(additional);
		stat.directMarketerPayment = Math.max(
			0, share * stat.additionalRevenues);
		stat.revenuesElectricity = stat.feedInFundingRevenues
			+ stat.referenceMarketRevenues
			+ stat.additionalRevenues
			- stat.directMarketerPayment;
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
	}

	/// The heat that is generated by the plant in kWh/a.
	private double generatedHeat() {
		return revenues != null ? revenues.totalGeneratedHeat() : 0;
	}

	/// The heat that can be sold to the heat network in kWh/a. The shares for
	/// heating the fermenter and for unused heat are subtracted from the
	/// generated heat.
	private double sellableHeat() {
		double share = Math.min(1,
			heatShare(plant.settings.fermenterHeatShare)
				+ heatShare(plant.settings.unusedHeatShare));
		return generatedHeat() * (1 - share);
	}

	/// The heat that is not used and must be cooled away in kWh/a.
	private double unusedHeat() {
		return generatedHeat() * heatShare(plant.settings.unusedHeatShare);
	}

	/// Converts a percentage value to a fraction and clamps it to 0..100 %.
	private static double heatShare(double share) {
		return Math.clamp(share, 0, 100) / 100.0;
	}

	/// Sets the heat revenues and the total revenues of the dynamic and the
	/// static summary. The heat revenues are calculated from the suggested heat
	/// price and the heat that can be sold to the heat network. The total
	/// revenues are the sum of the annual funding, the electricity revenues, and
	/// the heat revenues.
	private void addRevenuesHeat(BiogasCostResult r) {
		double soldHeat = sellableHeat();
		setHeatRevenues(r.dynamicTotal, soldHeat, true);
		setHeatRevenues(r.staticTotal, soldHeat, false);
	}

	private void setHeatRevenues(
		BiogasCostResult.Summary s, double soldHeat, boolean dynamic
	) {
		s.suggestedHeatPrice = suggestedHeatPrice(s, soldHeat, dynamic);
		s.revenuesHeat = s.suggestedHeatPrice * soldHeat / 1000.0;
		s.totalRevenues = s.annualFunding
			+ s.revenuesElectricity
			+ s.revenuesHeat;
	}

	/// The suggested heat price in EUR/MWh of the given summary. With
	/// `WP = 1000 * WE / WM` and `WE = target - annualSurplus`, where `WM` is
	/// the heat that can be sold to the heat network and the target is the
	/// surplus that is needed to reach the total capital return rate
	/// (`surplusForCapitalReturnRate`) in the capital return rate case and the
	/// expected annual surplus in the annual surplus case.
	private double suggestedHeatPrice(
		BiogasCostResult.Summary summary, double soldHeat, boolean dynamic
	) {
		if (soldHeat <= 0)
			return 0;
		var settings = plant.settings;
		double target = settings.useCapitalReturnRate
			? surplusForCapitalReturnRate(summary, dynamic)
			: settings.expectedAnnualSurplus;
		double annualSurplus = summary.annualFunding
			+ summary.revenuesElectricity
			- summary.totalAnnualCosts;
		double heatRevenue = target - annualSurplus;
		// a negative heat price would mean that the plant pays for the heat
		// that it delivers, so we clamp it at 0
		return Math.max(0, 1000 * heatRevenue / soldHeat);
	}

	/// The annual surplus (Jahresüberschuss) that is needed so that the total
	/// capital return rate (Gesamtkapitalrentabilität) of the given summary
	/// reaches the expected value of the plant settings:
	///
	/// - static: `GKR = (JÜ + i * I) / I`
	/// - dynamic: `GKR = (JÜ + 0,5 * i * I) / (I / 2)`
	///
	/// with `I = investments - investmentFunding` and `i = interestRate / 100`
	/// (Kapital-Mischzinssatz). Resolved for the annual surplus this is
	/// `I * (GKR - i)` for the static and `I / 2 * (GKR - i)` for the dynamic
	/// calculation.
	private double surplusForCapitalReturnRate(
		BiogasCostResult.Summary summary, boolean dynamic
	) {
		double capital = summary.investments - summary.investmentFunding;
		if (dynamic) {
			capital /= 2.0;
		}
		double rate = plant.settings.capitalReturnRate / 100.0;
		double interest = plant.settings.interestRate / 100.0;
		return capital * (rate - interest);
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
