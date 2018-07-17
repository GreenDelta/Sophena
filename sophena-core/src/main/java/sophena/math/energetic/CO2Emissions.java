package sophena.math.energetic;

import java.util.HashMap;
import java.util.Map;

import sophena.Defaults;
import sophena.calc.ConsumerResult;
import sophena.calc.EnergyResult;
import sophena.calc.ProjectResult;
import sophena.model.Fuel;
import sophena.model.Producer;
import sophena.model.Project;

public class CO2Emissions {

	public double electricityEmissions;
	public double electricityCredits;
	public double total;
	public double variantOil;
	public double variantNaturalGas;
	public final Map<Producer, Double> producerEmissions = new HashMap<>();

	public static CO2Emissions calculate(ProjectResult result) {
		return new Calculator(result).calculate();
	}

	private CO2Emissions() {
	}

	private static class Calculator {

		final Project project;
		final ProjectResult result;

		Calculator(ProjectResult result) {
			this.project = result.project;
			this.result = result;
		}

		CO2Emissions calculate() {
			CO2Emissions co2 = new CO2Emissions();
			if (result == null)
				return co2;
			EnergyResult eResult = result.energyResult;
			// order is important !!!
			addElectricityEmissions(co2);
			addElectrivityCredits(co2, eResult);
			addEmissions(co2, eResult);
			double heatDemand = getTotalHeatDemand();
			co2.variantNaturalGas = (heatDemand / 0.95)
					* Defaults.EMISSION_FACTOR_NATURAL_GAS;
			co2.variantOil = (heatDemand / 0.92) * Defaults.EMISSION_FACTOR_OIL;
			return co2;
		}

		private void addElectrivityCredits(CO2Emissions co2,
				EnergyResult eResult) {
			double e = GeneratedElectricity.getTotal(eResult);
			co2.electricityCredits = e * Defaults.EMISSION_FACTOR_ELECTRICITY;
		}

		private void addElectricityEmissions(CO2Emissions co2) {
			if (result == null || result.energyResult == null
					|| project == null)
				return;
			EnergyResult eResult = result.energyResult;
			double used = UsedElectricity.get(eResult.totalProducedHeat,
					project.costSettings);
			co2.electricityEmissions = used
					* Defaults.EMISSION_FACTOR_ELECTRICITY;
		}

		private void addEmissions(CO2Emissions co2, EnergyResult eResult) {
			co2.total = co2.electricityEmissions - co2.electricityCredits;
			for (Producer p : eResult.producers) {
				double demand = result.fuelUsage.getInKWh(p);
				double kg = demand * getEmissionFactor(p) / 1000;
				co2.producerEmissions.put(p, kg);
				co2.total += kg;
			}
		}

		private double getEmissionFactor(Producer p) {
			if (p == null || p.fuelSpec == null)
				return 0;
			Fuel fuel = p.fuelSpec.fuel;
			return fuel != null ? fuel.co2Emissions : 0d;
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
