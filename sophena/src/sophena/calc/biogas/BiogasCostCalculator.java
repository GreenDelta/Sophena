package sophena.calc.biogas;

import sophena.calc.costs.Annuity;
import sophena.calc.costs.CostResult;
import sophena.calc.costs.InvestmentItem;
import sophena.calc.costs.Investments;
import sophena.model.AnnualCostEntry;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.SubstrateProfile;

/**
 * Calculator for the economic evaluation of a biogas plant.
 *
 * This class calculates annual costs (capital, consumption, operation) and
 * revenues from electricity sales based on the VDI 2067 annuity method.
 * All calculations consider the plant-specific duration, interest rate,
 * and price change factors.
 */
public class BiogasCostCalculator {

	private final BiogasPlant plant;
	private final BiogasRuntimeResult result;

	/**
	 * Creates a new calculator for the given plant and its energy results.
	 *
	 * @param plant The biogas plant model with cost settings.
	 * @param result The simulation result containing run flags and energy data.
	 */
	public BiogasCostCalculator(BiogasPlant plant, BiogasRuntimeResult result) {
		this.plant = plant;
		this.result = result;
	}

	/**
	 * Calculates the economic performance of the biogas plant.
	 *
	 * @return A FieldSet containing the categorized annual costs and revenues.
	 */
	public CostResult.FieldSet calculate() {
		var fs = new CostResult.FieldSet();

		if (plant == null) {
			return fs;
		}

		// Initial investment recorded for reference
		fs.investments = BiogasPlants.totalInvestment(plant);

		// 1. Capital Costs: The annual annuity of the investment
		fs.capitalCosts = calculateCapitalCosts();

		// 2. Consumption Costs: Biomass substrate costs and purchased grid electricity
		fs.consumptionCosts = calculateConsumptionCosts();

		// 3. Operation Costs: Labor (wages), maintenance/repair, and insurance
		fs.operationCosts = calculateOperationCosts();

		// 4. Other Annual Costs: Fixed costs like administration or laboratory fees
		fs.otherAnnualCosts = calculateOtherAnnualCosts();

		// Sum up all cost categories to get the total annual expenditure
		fs.totalAnnualCosts = fs.capitalCosts + fs.consumptionCosts + fs.operationCosts + fs.otherAnnualCosts;

		// 5. Revenues: Income from electricity feed-in
		fs.revenuesElectricity = calculateRevenues();

		// Final economic result: Annual surplus (revenues - costs)
		fs.annualSurplus = fs.revenuesElectricity - fs.totalAnnualCosts;

		return fs;
	}

	/**
	 * Calculates the capital cost annuity using the VDI 2067 methodology.
	 * It assumes the observation period (T) is equal to the plant's duration.
	 */
	private double calculateCapitalCosts() {
		double sum = 0;
		int T = plant.duration;
		double ir = plant.settings.interestRate;
		double factor = plant.settings.investmentFactor;
		for (BiogasPlantBoiler entry : plant.boilers) {
			if (entry == null || entry.costs == null)
				continue;
			sum += Investments.capitalCosts(
				entry.costs.investment,
				entry.costs.investment,
				entry.costs.duration,
				T,
				ir,
				factor);
		}
		for (var entry : plant.investments) {
			sum += Investments.capitalCosts(
				InvestmentItem.of(entry), T, ir, factor);
		}
		return sum;
	}

	/**
	 * Summarizes costs for substrates and electricity purchased from the grid.
	 */
	private double calculateConsumptionCosts() {
		// Calculate base costs for all substrates (EUR/a)
		double substrateSum = 0;
		for (SubstrateProfile profile : plant.substrateProfiles) {
			substrateSum += profile.annualMass * profile.substrateCosts;
		}
		// Apply duration-based annuity factor for bio-fuels
		double bioAnnuity = annuity(substrateSum, plant.settings.bioFuelFactor);

		// Calculate electricity purchased from the grid (EUR/a)
		double electricitySum = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			// Grid power is needed if plant is in full feed-in mode OR if currently idle.
			// Base demand is required regardless of output.
			if (plant.settings.isFullFeedIn || !result.runFlags()[h]) {
				electricitySum += plant.settings.avgPowerDemand * plant.settings.electricityPrice;
			}
		}
		// Apply duration-based annuity factor for grid electricity
		double elecAnnuity = annuity(electricitySum, plant.settings.electricityFactor);

		return bioAnnuity + elecAnnuity;
	}

	/**
	 * Calculates operation-related costs including maintenance and labor.
	 */
	private double calculateOperationCosts() {
		// Maintenance/Repair: sum of all block-specific maintenance shares
		double maintBase = 0;
		for (BiogasPlantBoiler entry : plant.boilers) {
			if (entry == null || entry.costs == null)
				continue;
			maintBase += Investments.maintenanceBase(
				entry.costs.investment,
				entry.costs.repair,
				entry.costs.maintenance);
		}
		for (var entry : plant.investments) {
			// maintenance and repair are applied to the full investment
			maintBase += Investments.maintenanceBase(
				InvestmentItem.of(entry));
		}
		double maintAnnuity = annuity(maintBase, plant.settings.maintenanceFactor);

		// Labor: operating hours times hourly wage
		double operBase = BiogasPlants.totalOperationHours(plant) * plant.settings.hourlyWage;
		double operAnnuity = annuity(operBase, plant.settings.operationFactor);

		// Insurance: fixed percentage of the full investment value (assumed
		// constant price level)
		double insurance = BiogasPlants.totalInvestmentValue(plant)
			* (plant.settings.insuranceCostsShare / 100);

		return maintAnnuity + operAnnuity + insurance;
	}

	/**
	 * Totals all additional fixed cost entries.
	 */
	private double calculateOtherAnnualCosts() {
		double sum = 0;
		if (plant.otherAnnualCosts != null) {
			for (AnnualCostEntry entry : plant.otherAnnualCosts) {
				sum += entry.value;
			}
		}
		return sum;
	}

	/**
	 * Calculates revenues from electricity feed-in.
	 * Considers transmission losses and (if not full feed-in) self-consumption.
	 */
	private double calculateRevenues() {
		double hourlyRevenuesSum = 0;

		// Net electrical power available for feed-in
		double netPower = Math.max(0, BiogasPlants.totalElectricPower(plant) - plant.settings.transmissionLosses);

		// Subtract internal demand from production if not in full feed-in mode (surplus feed-in)
		if (!plant.settings.isFullFeedIn) {
			netPower = Math.max(0, netPower - plant.settings.avgPowerDemand);
		}

		for (int h = 0; h < Stats.HOURS; h++) {
			if (result.runFlags()[h]) {
				double price = 0;
				if (plant.electricityPrices != null && plant.electricityPrices.values != null) {
					// ElectricityPriceCurve values are stored in ct/kWh -> convert to EUR/kWh
					price = plant.electricityPrices.values[h] / 100.0;
				}
				// netPower (kW) * 1h * price (EUR/kWh) = earnings in EUR
				hourlyRevenuesSum += netPower * price;
			}
		}

		// Apply duration-based annuity factor for electricity revenues
		return annuity(hourlyRevenuesSum, plant.settings.electricityRevenuesFactor);
	}

	/// The annuity of the given first-year value for the observed duration of the
	/// plant, see `Costs.annuity`.
	private double annuity(double firstYearValue, double priceChangeFactor) {
		return Annuity.ofYearlyCosts(
			plant.duration,
			firstYearValue,
			plant.settings.interestRate,
			priceChangeFactor);
	}
}
