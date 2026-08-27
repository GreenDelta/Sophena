package sophena.calc.biogas.fermentersim;

import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;

record FermenterParameters(
	Fermenter fermenter,
	double bhkwMeanElectricPowerKW,
	double bhkwElectricEfficiency,
	double biogasMethaneFraction
) {

	static FermenterParameters of(BiogasPlant plant) {
		return new FermenterParameters(
			plant.fermenter,
			500.0,    // Mean CHP electric power [kW]
			0.40,     // CHP electric efficiency [-]
			0.50      // Methane fraction in biogas [-]
		);
	}
}
