package sophena.calc.biogas.fermentersim;


import sophena.model.WeatherStation;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;

/**
 * Operational parameters of the fermenter. The construction parameters are
 * taken directly from the linked fermenter instance; the location used for
 * the solar calculations is taken directly from the referenced weather station.
 */
public record FermenterParameters(
	Fermenter fermenter,
	WeatherStation station,
	double bhkwMeanElectricPowerKW,
	double bhkwElectricEfficiency,
	double biogasMethaneFraction,
	double membraneRoofAlpha,
	double liquidSurfaceEpsilon,
	double innerMembraneEpsilonInterior,
	double innerMembraneEpsilonGap,
	double outerMembraneEpsilonGap,
	double outerMembraneEpsilonExterior
) {

	public static FermenterParameters createDefault(WeatherStation station) {
		if (station == null) {
			throw new IllegalArgumentException("Weather station is required.");
		}

		var fermenter = new Fermenter();
		fermenter.roofType = RoofType.DOUBLE_MEMBRANE;
		fermenter.targetTemperature = 38.0;
		fermenter.wallOuterRadius = 12.32;
		fermenter.wallStructuralThickness = 0.10;
		fermenter.wallInsulationThickness = 0.10;
		fermenter.wallTotalHeight = 10.0;
		fermenter.wallBuriedFraction = 0.50;
		fermenter.roofFixedLayerThickness = 0.01;
		fermenter.roofInsulationThickness = 0.10;
		fermenter.roofMembraneHeight = 4.0;
		fermenter.floorSlabThickness = 0.20;
		fermenter.floorInsulationThickness = 0.10;
		fermenter.wallShadingFraction = 0.50;
		fermenter.roofShadingFraction = 0.50;
		fermenter.mixerPowerDensity = 16.0;
		fermenter.mixerRuntime = 15.0;
		fermenter.mixerHeatFraction = 1.0;

		return new FermenterParameters(
			fermenter,
			station,
			500.0,                      // Mean CHP electric power [kW]
			0.40,                       // CHP electric efficiency [-]
			0.50,                       // Methane fraction in biogas [-]
			0.60,                       // Solar absorption coefficient of outer membrane [-]
			0.95,                       // Substrate liquid surface emissivity [-]
			0.90,                       // Inner membrane emissivity (interior side) [-]
			0.90,                       // Inner membrane emissivity (gap side) [-]
			0.90,                       // Outer membrane emissivity (gap side) [-]
			0.90                        // Outer membrane emissivity (exterior side) [-]
		);
	}
}
