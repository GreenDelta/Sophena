package sophena.calc.biogas;

import sophena.calc.CostResult;
import sophena.model.AnnualCostEntry;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.SubstrateProfile;
import sophena.math.costs.CapitalCosts;

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
	private final BiogasPlantResult result;

	/**
	 * Creates a new calculator for the given plant and its energy results.
	 *
	 * @param plant The biogas plant model with cost settings.
	 * @param result The simulation result containing run flags and energy data.
	 */
	public BiogasCostCalculator(BiogasPlant plant, BiogasPlantResult result) {
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

		if (plant == null || plant.costs == null) {
			return fs;
		}

		// Initial investment recorded for reference
		fs.investments = plant.costs.investment;

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
		double q = 1 + plant.interestRate / 100;
		// calculate(Investment, service_life, observation_period, interest_factor, price_change_factor)
		return CapitalCosts.calculate(
				plant.costs.investment,
				plant.duration,
				plant.duration,
				q,
				plant.investmentFactor);
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
		double bioAnnuity = substrateSum * annuityFactor(plant.bioFuelFactor);

		// Calculate electricity purchased from the grid (EUR/a)
		double electricitySum = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			// Grid power is needed if plant is in full feed-in mode OR if currently idle.
			// Base demand is required regardless of output.
			if (plant.isFullFeedIn || !result.runFlags()[h]) {
				electricitySum += plant.electricityDemand * plant.electricityPrice;
			}
		}
		// Apply duration-based annuity factor for grid electricity
		double elecAnnuity = electricitySum * annuityFactor(plant.electricityFactor);

		return bioAnnuity + elecAnnuity;
	}

	/**
	 * Calculates operation-related costs including maintenance and labor.
	 */
	private double calculateOperationCosts() {
		// Maintenance/Repair: percentage of total investment
		double maintBase = plant.costs.investment * (plant.costs.maintenance + plant.costs.repair) / 100;
		double maintAnnuity = maintBase * annuityFactor(plant.maintenanceFactor);

		// Labor: operating hours times hourly wage
		double operBase = plant.costs.operation * plant.hourlyWage;
		double operAnnuity = operBase * annuityFactor(plant.operationFactor);

		// Insurance: fixed percentage of investment (assumed constant price level)
		double insurance = plant.costs.investment * (plant.insuranceShare / 100);

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
		double netPower = Math.max(0, plant.ratedPower - plant.transmissionLosses);

		// Subtract internal demand from production if not in full feed-in mode (surplus feed-in)
		if (!plant.isFullFeedIn) {
			netPower = Math.max(0, netPower - plant.electricityDemand);
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
		return hourlyRevenuesSum * annuityFactor(plant.electricityRevenuesFactor);
	}

	/**
	 * Helper method to calculate the combined annuity and price change factor (VDI 2067).
	 * a * b = (annuity factor) * (cash value factor for price changes)
	 *
	 * @param r Price change factor (e.g., 1.05 for 5% increase).
	 * @return Combined factor to multiply with first-year costs.
	 */
	private double annuityFactor(double r) {
		double q = 1 + plant.interestRate / 100;
		int T = plant.duration;
		if (T <= 0) return 0;

		// a: Annuity factor for capital recovery
		double a = (q - 1) / (1 - Math.pow(q, -T));

		// b: Cash value factor for the geometric series of price changes
		double b;
		if (Math.abs(r - q) < 0.000001) {
			b = T / q;
		} else {
			b = (1 - Math.pow(r / q, T)) / (q - r);
		}

		return a * b;
	}
}
