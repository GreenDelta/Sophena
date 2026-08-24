package sophena.calc.biogas.fermentersim;


import sophena.model.WeatherStation;
import sophena.model.biogas.RoofType;

/**
 * Plant operational parameters and geometry definitions. The location used for
 * the solar calculations is taken directly from the referenced weather station.
 */
public record FermenterParameters(
	double tSetC,
	double wRaM,
	double wS,
	double wIm,
	double wHTotalM,
	double buriedWallFraction,
	RoofType roofType,
	double dIm,
	double dSdM,
	double membraneRoofHeightM,
	double bhkwMeanElectricPowerKW,
	double boSM,
	double boIm,
	WeatherStation station,
	double wallShadingFraction,
	double roofShadingFraction,
	double mixerInstalledPowerDensityWm3,
	double mixerRunTimeMinPerHour,
	double mixerHeatFraction,
	double bhkwElectricEfficiency,
	double biogasMethaneFraction,
	double membraneRoofAlpha,
	double liquidSurfaceEpsilon,
	double innerMembraneEpsilonInterior,
	double innerMembraneEpsilonGap,
	double outerMembraneEpsilonGap,
	double outerMembraneEpsilonExterior,
	double wRim
) {

	public static FermenterParameters createDefault(WeatherStation station) {
		if (station == null) {
			throw new IllegalArgumentException("Weather station is required.");
		}
		double wRaM = 12.32;
		double wS = 0.10;
		double wIm = 0.10;
		double wRim = wRaM - wIm - wS;

		return new FermenterParameters(
			38.0,                       // Target substrate temperature [degC]
			wRaM,                       // Wall outer radius [m]
			wS,                         // Structural wall thickness [m]
			wIm,                        // Insulation thickness [m]
			10.0,                       // Total vertical wall height [m]
			0.50,                       // Fraction of wall buried underground [-]
			RoofType.DOUBLE_MEMBRANE,   // Roof type selection
			0.01,                       // Fixed roof layer thickness [m]
			0.10,                       // Fixed roof insulation thickness [m]
			4.0,                        // Membrane roof dome height [m]
			500.0,                      // Mean CHP electric power [kW]
			0.20,                       // Floor slab thickness [m]
			0.10,                       // Floor insulation thickness [m]
			station,                    // Weather station providing the location
			0.50,                       // Wall shading fraction [-]
			0.50,                       // Roof shading fraction [-]
			16.0,                       // Installed mixer power density [W/m3]
			15.0,                       // Mixer runtime per hour [min/h]
			1.0,                        // Mixer heat fraction in operation [-]
			0.40,                       // CHP electric efficiency [-]
			0.50,                       // Methane fraction in biogas [-]
			0.60,                       // Solar absorption coefficient of outer membrane [-]
			0.95,                       // Substrate liquid surface emissivity [-]
			0.90,                       // Inner membrane emissivity (interior side) [-]
			0.90,                       // Inner membrane emissivity (gap side) [-]
			0.90,                       // Outer membrane emissivity (gap side) [-]
			0.90,                       // Outer membrane emissivity (exterior side) [-]
			wRim                        // Calculated inner radius [m]
		);
	}
}
