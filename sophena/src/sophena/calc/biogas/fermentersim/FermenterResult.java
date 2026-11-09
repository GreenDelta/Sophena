package sophena.calc.biogas.fermentersim;

import java.util.List;

import sophena.model.biogas.Fermenter;

/// The simulation result of a single fermenter of a biogas plant.
///
/// @param fermenter the fermenter
/// @param steps the hourly simulation results (hour 0..8759)
/// @param totalEnergyMWh the annual heat demand of the fermenter in MWh
/// @param peakHeatingPowerKW the maximum hourly heat demand of the fermenter
///   in kW
public record FermenterResult(
	Fermenter fermenter,
	List<SimulationResultStep> steps,
	double totalEnergyMWh,
	double peakHeatingPowerKW
) {
}
