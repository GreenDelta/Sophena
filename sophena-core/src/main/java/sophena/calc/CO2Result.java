package sophena.calc;

import java.util.HashMap;
import java.util.Map;

import sophena.Defaults;
import sophena.math.energetic.GeneratedElectricity;
import sophena.math.energetic.UsedElectricity;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.Producer;
import sophena.model.Project;

/**
 * Contains the CO2 emissions result of a project.
 */
public class CO2Result {

	/**
	 * The CO2 emissions related to the use of electricity for generating the
	 * heat.
	 */
	public double electricityEmissions;

	public double electricityCredits;
	public double total;
	public double variantOil;
	public double variantNaturalGas;
	public final Map<Producer, Double> producerEmissions = new HashMap<>();

	static CO2Result calculate(ProjectResult result) {
		return new Calculator(result).calculate();
	}

	private CO2Result() {
	}

	private static class Calculator {

		final Project project;
		final ProjectResult result;

		Calculator(ProjectResult result) {
			this.project = result.project;
			this.result = result;
		}

		CO2Result calculate() {
			CO2Result co2 = new CO2Result();
			if (result == null)
				return co2;
			EnergyResult eResult = result.energyResult;
			addElectricityEmissions(co2);
			addElectrivityCredits(co2);
			co2.total = co2.electricityEmissions - co2.electricityCredits;
			addProducers(co2, eResult); // also adds to the total
			double heatDemand = getTotalHeatDemand();
			co2.variantNaturalGas = (heatDemand / 0.95)
					* Defaults.EMISSION_FACTOR_NATURAL_GAS;
			co2.variantOil = (heatDemand / 0.92)
					* Defaults.EMISSION_FACTOR_OIL;
			return co2;
		}

		private void addElectrivityCredits(CO2Result co2) {
			for (Producer p : result.energyResult.producers) {
				double e = GeneratedElectricity.get(p, result);
				if (e > 0) {
					double factor = factor(
							p.producedElectricity,
							Defaults.EMISSION_FACTOR_ELECTRICITY);
					co2.electricityCredits += (factor * e);
				}
			}
		}

		private void addElectricityEmissions(CO2Result co2) {
			if (result.energyResult == null
					|| project == null
					|| project.costSettings == null)
				return;
			CostSettings settings = project.costSettings;
			EnergyResult eResult = result.energyResult;
			double used = UsedElectricity.get(
					eResult.totalProducedHeat, settings);
			double factor = factor(settings.usedElectricity,
					Defaults.EMISSION_FACTOR_ELECTRICITY);
			co2.electricityEmissions = used * factor;
		}

		private void addProducers(CO2Result co2, EnergyResult eResult) {
			for (Producer p : eResult.producers) {
				if (p.fuelSpec == null)
					continue;
				double kWh = result.fuelUsage.getInKWh(p);
				double kgCO2 = kWh * factor(p.fuelSpec.fuel, 0);
				co2.producerEmissions.put(p, kgCO2);
				co2.total += kgCO2;
			}
		}

		/**
		 * Returns the emission factor for the given fuel in kg CO2 / kWh. If
		 * the given fuel is null it returns the default value.
		 */
		private double factor(Fuel f, double defaultVal) {
			if (f == null)
				return defaultVal;
			return f.co2Emissions / 1000;
		}

		private double getTotalHeatDemand() {
			double totalDemand = 0;
			for (ConsumerResult cr : result.consumerResults) {
				totalDemand += cr.heatDemand;
			}
			return totalDemand;
		}
	}
}
