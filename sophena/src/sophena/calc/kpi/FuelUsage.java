package sophena.calc.kpi;

import java.util.HashMap;

import sophena.calc.ProjectResult;
import sophena.calc.biogas.SubstrateInput;
import sophena.calc.specs.CalorificValue;
import sophena.calc.specs.Producers;
import sophena.calc.specs.UtilisationRate;
import sophena.model.FuelSpec;
import sophena.model.Producer;

public class FuelUsage {

	private final HashMap<String, Double> inKWh = new HashMap<>();
	private final HashMap<String, Double> inFuelUnits = new HashMap<>();

	public static FuelUsage calculate(ProjectResult r) {
		var usage = new FuelUsage();
		if (r == null || r.project == null)
			return usage;
		if (r.energyResult == null)
			return usage;
		for (Producer p : r.project.producers) {
			double inKWh;
			double amount;
			var biogas = r.biogasResults.get(p);
			if (biogas != null) {
				// the fuel of a biogas plant is the input of its substrates
				var input = SubstrateInput.of(biogas.plant());
				inKWh = input.fuelEnergy();
				amount = input.mass();
			} else {
				inKWh = calcKWh(r, p);
				amount = calcAmount(p, inKWh);
			}
			usage.inKWh.put(p.id, inKWh);
			usage.inFuelUnits.put(p.id, amount);
		}
		return usage;
	}

	private static double calcKWh(ProjectResult r, Producer producer) {
		double generatedHeat = r.energyResult.totalHeat(producer);
		if (producer.heatPump != null) {
			var jaz = r.energyResult.jaz(producer);
			if (jaz != 0)
				return generatedHeat / jaz;
			return 0;
		}

		double electricalEfficiency = Producers.electricalEfficiency(producer);
		if (electricalEfficiency <= 0) {
			double ur = UtilisationRate.get(r.project, producer, r.energyResult);
			return ur == 0 ? 0 : generatedHeat / ur;
		} else {
			double tf = Producers.fullLoadHours(producer, generatedHeat);
			double powerEl = Producers.electricPower(producer);
			double Pf = powerEl / electricalEfficiency;
			return Pf * tf;
		}
	}

	private static double calcAmount(Producer producer, double inKWh) {
		FuelSpec spec = producer.fuelSpec;
		double cv = CalorificValue.get(spec);
		return cv == 0 ? 0 : inKWh / cv;
	}

	/**
	 * Get the amount of fuel in the respective fuel unit to produce the given
	 * heat by the given producer.
	 */
	public double getInFuelUnits(Producer p) {
		if (p == null)
			return 0;
		Double val = inFuelUnits.get(p.id);
		return val == null ? 0.0 : val;
	}

	/**
	 * Get the amount of fuel energy in [kWh] that is required to produce the
	 * given amount of heat by the given producer.
	 */
	public double getInKWh(Producer p) {
		if (p == null)
			return 0;
		Double val = inKWh.get(p.id);
		return val == null ? 0.0 : val;
	}
}
