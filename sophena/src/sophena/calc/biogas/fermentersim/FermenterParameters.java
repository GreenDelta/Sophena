package sophena.calc.biogas.fermentersim;

import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;

record FermenterParameters(
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

	static FermenterParameters of(BiogasPlant plant, WeatherStation station) {
		return new FermenterParameters(
			plant.fermenter,
			station,
			500.0,    // Mean CHP electric power [kW]
			0.40,     // CHP electric efficiency [-]
			0.50,     // Methane fraction in biogas [-]
			0.60,     // Solar absorption coefficient of outer membrane [-]
			0.95,     // Substrate liquid surface emissivity [-]
			0.90,     // Inner membrane emissivity (interior side) [-]
			0.90,     // Inner membrane emissivity (gap side) [-]
			0.90,     // Outer membrane emissivity (gap side) [-]
			0.90      // Outer membrane emissivity (exterior side) [-]
		);
	}
}
