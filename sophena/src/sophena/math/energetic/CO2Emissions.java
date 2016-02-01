package sophena.math.energetic;

import java.util.HashMap;
import java.util.Map;

import sophena.Defaults;
import sophena.calc.ConsumerResult;
import sophena.calc.EnergyResult;
import sophena.calc.ProjectResult;
import sophena.model.Producer;

public class CO2Emissions {

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

		final ProjectResult result;

		Calculator(ProjectResult result) {
			this.result = result;
		}

		CO2Emissions calculate() {
			CO2Emissions co2 = new CO2Emissions();
			if (result == null)
				return co2;
			EnergyResult eResult = result.energyResult;
			addElectrivityCredits(co2, eResult);
			addEmissions(co2, eResult);
			double heatDemand = getHeatDemand();
			co2.variantNaturalGas = (heatDemand / 0.95) * Defaults.EMISSION_FACTOR_NATURAL_GAS;
			co2.variantOil = (heatDemand / 0.92) * Defaults.EMISSION_FACTOR_OIL;
			return co2;
		}

		private void addElectrivityCredits(CO2Emissions co2, EnergyResult eResult) {
			double e = GeneratedElectricity.getTotalkWh(eResult);
			co2.electricityCredits = e * Defaults.EMISSION_FACTOR_ELECTRICITY;
		}

		private void addEmissions(CO2Emissions co2, EnergyResult eResult) {
			co2.total = -co2.electricityCredits;
			for (Producer p : eResult.producers) {
				double genHeat = eResult.totalHeat(p);
				double demand = FuelDemand.getKWh(p, genHeat);
				double kg = demand * getEmissionFactor(p) / 1000;
				co2.producerEmissions.put(p, kg);
				co2.total += kg;
			}
		}

		private double getEmissionFactor(Producer producer) {
			if (producer == null || producer.boiler == null)
				return 0;
			if (producer.boiler.fuel != null)
				return producer.boiler.fuel.co2Emissions;
			if (producer.fuelSpec != null && producer.fuelSpec.woodFuel != null)
				return producer.fuelSpec.woodFuel.co2Emissions;
			else
				return 0;
		}

		private double getHeatDemand() {
			double totalDemand = 0;
			for (ConsumerResult cr : result.consumerResults) {
				totalDemand += cr.heatDemand;
			}
			return totalDemand;
		}
	}
}
