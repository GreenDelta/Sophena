package sophena.calc.kpi;

import java.util.HashMap;
import java.util.Map;

import sophena.Defaults;
import sophena.calc.ProjectResult;
import sophena.calc.simulation.EnergyResult;
import sophena.model.Fuel;
import sophena.model.Producer;
import sophena.model.Project;

/// The CO2 eq. emissions of a project.
public class EmissionResult {

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

	public static EmissionResult calculate(ProjectResult result) {
		return result == null || result.energyResult == null
			? new EmissionResult()
			: new Calculator(result).calculate();
	}

	private EmissionResult() {
	}

	private static class Calculator {

		final Project project;
		final ProjectResult result;
		final EnergyResult energyResult;

		Calculator(ProjectResult result) {
			this.project = result.project;
			this.result = result;
			this.energyResult = result.energyResult;
		}

		EmissionResult calculate() {
			double fromUsedElectricity = usedElectricityEmissions();
			double electricityCredits = electricityCredits();
			double total = fromUsedElectricity - electricityCredits;

			var r = new EmissionResult();
			for (var p : energyResult.producers) {
				if (p.fuelSpec == null)
					continue;
				double fuelEnergy = result.fuelUsage.getInKWh(p);
				double fuelEmissions = fuelEnergy * factor(p.fuelSpec.fuel, 0);
				r.producerEmissions.put(p, fuelEmissions);
				total += fuelEmissions;
			}

			r.total = total;
			r.electricityCredits = electricityCredits;
			r.electricityEmissions = fromUsedElectricity;

			double demand = getConsumerHeatDemand();
			r.variantNaturalGas = (demand / 0.95)
				* Defaults.EMISSION_FACTOR_NATURAL_GAS;
			r.variantOil = (demand / 0.92)
				* Defaults.EMISSION_FACTOR_OIL;
			return r;
		}


		/// Collect the emission credits related to generated electricity.
		private double electricityCredits() {
			double credits = 0;
			for (var p : energyResult.producers) {
				double generated = GeneratedElectricity.get(p, result);
				if (generated <= 0)
					continue;
				double factor = factor(
					p.producedElectricity, Defaults.EMISSION_FACTOR_ELECTRICITY);
				credits += (factor * generated);
			}
			return credits;
		}

		private double usedElectricityEmissions() {
			if (project.costSettings == null)
				return 0;
			var settings = project.costSettings;
			double used = UsedElectricity.get(
				energyResult.totalProducedHeat, settings);
			double factor = factor(settings.projectElectricityMix,
				Defaults.EMISSION_FACTOR_ELECTRICITY);
			return used * factor;
		}

		/// Returns the emission factor for the given fuel in kg CO2 / kWh, or the
		/// default value when the fuel is `null`.
		private double factor(Fuel f, double defaultVal) {
			return f != null
				? f.co2Emissions / 1000
				: defaultVal;
		}

		private double getConsumerHeatDemand() {
			double totalDemand = 0;
			for (var cr : result.consumerResults) {
				totalDemand += cr.heatDemand;
			}
			return totalDemand;
		}
	}
}
