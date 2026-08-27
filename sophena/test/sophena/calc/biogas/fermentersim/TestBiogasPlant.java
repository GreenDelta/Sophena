package sophena.calc.biogas.fermentersim;

import java.util.Arrays;

import org.jspecify.annotations.NonNull;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;
import sophena.model.biogas.SubstrateProfile;

class TestBiogasPlant {

	private TestBiogasPlant() {
	}

	static BiogasPlant get() {
		var plant = new BiogasPlant();
		plant.fermenter =  makeFermenter();

		var profile = new SubstrateProfile();
		profile.hourlyValues = new double[Stats.HOURS];
		Arrays.fill(profile.hourlyValues, 1.875);
		plant.substrateProfiles.add(profile);

		return plant;
	}

	private static Fermenter makeFermenter() {
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
		return fermenter;
	}

}
