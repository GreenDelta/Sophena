package sophena.calc.biogas.fermentersim;

/**
 * Hourly simulation output variables for a single timestep (hour 0..8759).
 */
public record SimulationResultStep(
	int hour,
	double tAirC,
	double qHeatKW,
	double qRoofW,
	double qWallAirW,
	double qWallEarthW,
	double qFloorW,
	double qFeedW,
	double qMixerGainW,
	double tsRoofC,
	double tsInnerMembraneC,
	double tsSupportAirC,
	double tsWallC,
	double qInnerConvectionW,
	double qInnerRadiationW,
	double qGapInnerConvectionW,
	double qGapOuterConvectionW,
	double qMembraneRadiationW,
	double qSupportAirAdvectionW
) {
}
