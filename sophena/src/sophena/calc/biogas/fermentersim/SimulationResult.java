package sophena.calc.biogas.fermentersim;

import java.util.List;

/**
 * Complete simulation result including time series and overall metrics.
 */
public record SimulationResult(
	List<SimulationResultStep> steps,
	double totalEnergyMWh,
	double peakHeatingPowerKW,
	FermenterParameters parameters,
	MaterialConstants materials,
	SimulationConstants constants
) {
}
