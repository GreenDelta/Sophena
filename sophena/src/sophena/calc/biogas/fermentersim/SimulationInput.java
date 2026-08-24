package sophena.calc.biogas.fermentersim;


import sophena.model.Stats;

/**
 * Weather and operational input time series for a full year of 8760 hours.
 */
public record SimulationInput(
	double[] tAirC,
	double[] bHorWm2,
	double[] dHorWm2,
	double[] windMps,
	double[] feedKgH
) {

	public SimulationInput {
		if (tAirC.length != Stats.HOURS || bHorWm2.length != Stats.HOURS
			|| dHorWm2.length != Stats.HOURS || windMps.length != Stats.HOURS
			|| feedKgH.length != Stats.HOURS) {
			throw new IllegalArgumentException(
				"All input arrays must have exactly " + Stats.HOURS + " entries.");
		}
	}

	public int size() {
		return Stats.HOURS;
	}
}
