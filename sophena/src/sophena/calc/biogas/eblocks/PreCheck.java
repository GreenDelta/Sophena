package sophena.calc.biogas.eblocks;

import org.openlca.commons.Res;

import sophena.model.biogas.BiogasPlant;

class PreCheck {

	static Res<Void> validate(BiogasPlant plant) {
		if (plant == null)
			return Res.error("no-plant");
		if (plant.electricityPrices == null)
			return Res.error("no-electricity-prices");
		if (plant.minimumRuntime < 2 || plant.minimumRuntime > 12)
			return Res.error("invalid-min-runtime");

		// TODO add more validation checks

		return Res.ok();
	}

}
