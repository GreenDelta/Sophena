package sophena.calc.biogas.eblocks;

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;

import sophena.model.Boiler;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

/// Creates biogas plants for the tests of this package.
///
/// The plant has one boiler with 500 kW electric power and an electric
/// efficiency of 40 %, so it needs 1250 kW of fuel under full load. Its
/// substrate profile produces 200 m3 of biogas per hour with 50 % methane,
/// which is 997 kWh per hour or 0.8 hours of full load.
///
/// Because the plant produces less gas per hour than it needs under full load,
/// it can only run when the storage is filled. A storage of 1600 m3 is full
/// after 8 hours of production and holds 6.38 hours of full load.
final class TestPlant {

	/// The gas that the substrates produce in an hour, in m3.
	static final double GAS_PER_HOUR = 200;

	/// The methane content of the produced gas.
	static final double METHANE_CONTENT = 0.5;

	private TestPlant() {
	}

	/// A plant with the given gas storage size and minimum runtime, and a price
	/// of 10 ct/kWh in every hour of the year.
	static BiogasPlant of(double storageSize, int minimumRuntime) {
		return of(storageSize, minimumRuntime, hour -> 10);
	}

	/// A plant with the given gas storage size and minimum runtime, and the
	/// prices that the given function creates for an hour of the year.
	static BiogasPlant of(
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

	/// Blocks the feed-in of the plant in the given hours.
	static void blockFeedIn(BiogasPlant plant, int... hours) {
		var allowed = new boolean[Stats.HOURS];
		Arrays.fill(allowed, true);
		for (int hour : hours) {
			allowed[hour] = false;
		}
		plant.electricityPrices.feedInAllowed = allowed;
	}

	private static BiogasPlant base(double storageSize, int minimumRuntime) {
		// the fuel power under full load is 500 / 0.4 = 1250 kW
		var boiler = new Boiler();
		boiler.maxPowerElectric = 500;
		boiler.efficiencyRateElectric = 0.4;
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
		plant.minimumRuntime = minimumRuntime;
		return plant;
	}
}
