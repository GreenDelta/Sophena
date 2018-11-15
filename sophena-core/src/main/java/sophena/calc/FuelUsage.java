package sophena.calc;

import java.util.HashMap;

import sophena.math.energetic.CalorificValue;
import sophena.math.energetic.Producers;
import sophena.math.energetic.UtilisationRate;
import sophena.model.Boiler;
import sophena.model.FuelSpec;
import sophena.model.Producer;

public class FuelUsage {

	private final HashMap<String, Double> inKWh = new HashMap<>();
	private final HashMap<String, Double> inFuelUnits = new HashMap<>();

	static FuelUsage calculate(ProjectResult r) {
		FuelUsage usage = new FuelUsage();
		r.calcLog.h3("Brennstoffverbräuche");
		if (r == null || r.project == null)
			return usage;
		if (r.energyResult == null) {
			r.calcLog.println("FEHLER: kein energetisches Ergebnis\n");
			return usage;
		}
		for (Producer p : r.project.producers) {
			r.calcLog.println("=> Erzeuger: " + p.name);
			double inKWh = calcKWh(r, p);
			double amount = calcAmount(r, p, inKWh);
			usage.inKWh.put(p.id, inKWh);
			usage.inFuelUnits.put(p.id, amount);
			r.calcLog.println();
		}
		return usage;
	}

	private static double calcKWh(ProjectResult r, Producer producer) {
		double Qgen = r.energyResult.totalHeat(producer);
		r.calcLog.value("Qgen: erzeugte Wärme", Qgen, "KWh");
		Boiler boiler = producer.boiler;
		if (boiler == null || !boiler.isCoGenPlant) {
			double ur = UtilisationRate.get(r.project, producer,
					r.energyResult);
			r.calcLog.value("ur: Nutzungsgrad", ur, "");
			double val = ur == 0 ? 0 : Qgen / ur;
			r.calcLog.value("E: Benötigte Brennstoffenergie: E = Qgen / ur",
					val, "kWh");
			return val;
		} else {
			double tf = Producers.fullLoadHours(producer, Qgen);
			r.calcLog.value("tf: Volllaststunden", tf, "h");
			double er = boiler.efficiencyRateElectric;
			r.calcLog.value("er: elektrischer Wirkungsgrad", er, "");
			r.calcLog.value("Pe: elektrische Leistung",
					boiler.maxPowerElectric, "kW");
			double Pf = er == 0 ? 0 : boiler.maxPowerElectric / er;
			r.calcLog.value("Pf: Feuerungswärmeleistung: Pf = Pe / er",
					Pf, "kW");
			double val = Pf * tf;
			r.calcLog.value("E: Benötigte Brennstoffenergie: E = Pf * tf",
					val, "kWh");
			return val;
		}
	}

	private static double calcAmount(ProjectResult r, Producer producer,
			double inKWh) {
		FuelSpec spec = producer.fuelSpec;
		double cv = CalorificValue.get(spec);
		String fuelUnit = spec != null ? spec.getUnit() : "?";
		r.calcLog.value("cv: Heizwert", cv, "kWh/" + fuelUnit);
		double amount = cv == 0 ? 0 : inKWh / cv;
		r.calcLog.value("af: Brennstoffmenge: af = E / cv", amount, fuelUnit);
		return amount;
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
