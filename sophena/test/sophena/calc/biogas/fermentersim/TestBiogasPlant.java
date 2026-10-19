package sophena.calc.biogas.fermentersim;

import java.util.Arrays;

import sophena.calc.biogas.TestPlant;
import sophena.model.Boiler;
import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

class TestBiogasPlant {

	private TestBiogasPlant() {
	}

	static BiogasPlant get(WeatherStation station) {
		var plant = new BiogasPlant();
		plant.fermenter = makeFermenter();
		plant.boilers.add(makeBoiler());

		var profile = new SubstrateProfile();
		profile.substrate = new Substrate();
		profile.substrate.methaneContent = 50.0;
		profile.substrate.dryMatter = 10.0;
		profile.substrate.organicDryMatter = 47.5;
		profile.substrate.minTemperature = Stats.min(station.data);
		profile.substrate.maxTemperature = Stats.max(station.data);
		profile.hourlyValues = new double[Stats.HOURS];
		Arrays.fill(profile.hourlyValues, 1.875);
		plant.substrateProfiles.add(profile);

		return plant;
	}

	private static BiogasPlantBoiler makeBoiler() {
		var boiler = new Boiler();
		boiler.maxPowerElectric = 500.0;
		boiler.minPowerElectric = 500.0;
		boiler.efficiencyRateElectric = 0.4;
		var block = new BiogasPlantBoiler();
		block.boiler = boiler;
		return block;
	}

	private static Fermenter makeFermenter() {
		return TestPlant.fermenter();
	}

}
