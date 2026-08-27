package sophena.calc.biogas.fermentersim;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;

record FermenterParameters(
	Fermenter fermenter,
	double biogasMethaneFraction
) {

	static FermenterParameters of(BiogasPlant plant) {
		return new FermenterParameters(
			plant.fermenter,
			0.50      // Methane fraction in biogas [-]
		);
	}
}
