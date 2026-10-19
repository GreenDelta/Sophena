package sophena.calc.biogas;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

import sophena.model.Boiler;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

/// Creates biogas plants for the tests of the biogas calculation.
///
/// The plant has one boiler with 500 kW electric power and an electric
/// efficiency of 40 %, so it needs 1250 kW of fuel under full load. Its
/// substrate profile produces 200 m3 of biogas per hour with 50 % methane,
/// which is 997 kWh per hour or 0.8 hours of full load.
///
/// Because the plant produces less gas per hour than it needs under full load,
/// it can only run when the storage is filled. A storage of 1600 m3 is full
/// after 8 hours of production and holds 6.38 hours of full load.
public final class TestPlant {

	/// The gas that the substrates produce in an hour, in m3.
	public static final double GAS_PER_HOUR = 200;

	/// The methane content of the produced gas.
	public static final double METHANE_CONTENT = 0.5;

	private TestPlant() {
	}

	/// A plant with the given gas storage size and minimum runtime, and a price
	/// of 10 ct/kWh in every hour of the year.
	public static BiogasPlant of(double storageSize, int minimumRuntime) {
		return of(storageSize, minimumRuntime, _ -> 10);
	}

	/// A plant with the given gas storage size and minimum runtime, and the
	/// prices that the given function creates for an hour of the year.
	public static BiogasPlant of(
		double storageSize, int minimumRuntime, IntToDoubleFunction priceFn
	) {
		var plant = base(storageSize, minimumRuntime);
		var prices = new ElectricityPriceCurve();
		prices.values = new double[Stats.HOURS];
		for (int h = 0; h < Stats.HOURS; h++) {
			prices.values[h] = priceFn.applyAsDouble(h);
		}
		plant.electricityPrices = prices;
		return plant;
	}

	/// A valid fermenter that can be used in the tests.
	public static Fermenter fermenter() {
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

	/// Blocks the feed-in of the plant in the given hours.
	public static void blockFeedIn(BiogasPlant plant, int... hours) {
		var allowed = new boolean[Stats.HOURS];
		Arrays.fill(allowed, true);
		for (int hour : hours) {
			allowed[hour] = false;
		}
		plant.electricityPrices.feedInAllowed = allowed;
	}

	/// The fed-in electricity in each hour in kWh for the given run flags:
	/// full-load hours contribute the electric power of the plant (minus the
	/// internal power demand in the surplus feed-in mode), each ramp 1/8 of the
	/// power, and the cable and transformer losses are subtracted from every
	/// hour that feeds in.
	public static double[] feedInProfile(BiogasPlant plant, boolean[] flags) {
		double power = BiogasPlantCHP.of(plant).electricPower();
		double demand = plant.settings.avgPowerDemand;
		double losses = plant.settings.transmissionLosses;
		boolean fullFeedIn = plant.settings.isFullFeedIn;
		var feedIn = new double[flags.length];
		for (int h = 0; h < flags.length; h++) {
			if (flags[h]) {
				double energy = fullFeedIn ? power : power - demand;
				feedIn[h] = Math.max(0, energy - losses);
				continue;
			}
			int ramps = 0;
			if (h + 1 < flags.length && flags[h + 1])
				ramps++;
			if (h > 0 && flags[h - 1])
				ramps++;
			if (ramps > 0) {
				feedIn[h] = Math.max(0, ramps * power / 8.0 - losses);
			}
		}
		return feedIn;
	}

	private static BiogasPlant base(double storageSize, int minimumRuntime) {
		// the fuel power under full load is 500 / 0.4 = 1250 kW
		var boiler = new Boiler();
		boiler.maxPowerElectric = 500;
		boiler.efficiencyRateElectric = 0.4;
		// a plant needs valid thermal data as well, see `Validation`
		boiler.maxPower = 600;
		boiler.efficiencyRate = 0.4;
		var plantBoiler = new BiogasPlantBoiler();
		plantBoiler.boiler = boiler;

		// 8 t/h with a biogas yield of 25 m3 per t of organic dry matter
		// -> 200 m3 per hour with 50 % methane
		var substrate = new Substrate();
		substrate.dryMatter = 10;
		substrate.organicDryMatter = 50;
		substrate.biogasProduction = 500;
		substrate.methaneContent = METHANE_CONTENT * 100;

		var profile = new SubstrateProfile();
		profile.substrate = substrate;
		profile.hourlyValues = new double[Stats.HOURS];
		Arrays.fill(profile.hourlyValues, GAS_PER_HOUR / 25.0);

		var plant = new BiogasPlant();
		plant.boilers.add(plantBoiler);
		plant.substrateProfiles.add(profile);
		plant.gasStorageSize = storageSize;
		plant.gasStorageFillingLevel = 100;
		plant.minimumRuntime = minimumRuntime;
		// valid default settings, but without a methane slip so that the
		// produced gas volume is exactly `GAS_PER_HOUR`
		plant.settings = BiogasPlantSettings.createDefault(null);
		plant.settings.methaneSlip = 0;
		return plant;
	}
}
