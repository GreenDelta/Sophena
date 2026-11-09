package sophena.calc.biogas.fermentersim;

import java.util.List;

/// The result of the fermenter simulation of a biogas plant.
///
/// @param fermenters the results of the single fermenters, in the order in
///   which the substrate flows through the fermenters
/// @param heatDemand the hourly heat demand of all fermenters in kW
/// @param totalEnergyMWh the annual heat demand of all fermenters in MWh
/// @param peakHeatingPowerKW the maximum of the hourly heat demand of all
///   fermenters in kW
public record SimulationResult(
	List<FermenterResult> fermenters,
	double[] heatDemand,
	double totalEnergyMWh,
	double peakHeatingPowerKW
) {
}
