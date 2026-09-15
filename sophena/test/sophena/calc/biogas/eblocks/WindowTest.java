package sophena.calc.biogas.eblocks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

public class WindowTest {

	@Test
	public void windowFromInitialState() {
		var plant = plant(1600);
		var window = Window.of(State.initial(plant));

		// 200 m3/h are produced and the storage takes 8 hours of it
		assertEquals(0, window.startHour());
		assertEquals(8, window.fillHour());

		// a full storage holds 1600 * 0.5 * 9.97 = 7976 kWh which is
		// 6.38 full load hours; as the plant produces 997 kWh per hour
		// while it runs, the run is much longer than that
		assertEquals(38, window.emptyHour());
		assertEquals(30, window.maxRunHours());
	}

	@Test
	public void windowWhenStorageNeverFills() {
		// the storage (2_000_000 m3) is larger than the annual production
		// of 200 * 8760 = 1_752_000 m3
		var plant = plant(2_000_000);
		var window = Window.of(State.initial(plant));

		assertEquals(Stats.HOURS, window.fillHour());
		assertEquals(0, window.maxRunHours());
		assertTrue(window.isEmpty());
	}

	private static BiogasPlant plant(double storageSize) {
		// the fuel power under full load is 500 / 0.4 = 1250 kW
		var boiler = new Boiler();
		boiler.maxPowerElectric = 500;
		boiler.efficiencyRateElectric = 0.4;
		var plantBoiler = new BiogasPlantBoiler();
		plantBoiler.boiler = boiler;

		// 8 t/h with a biogas yield of 25 m3/t -> 200 m3/h of 50% methane
		var substrate = new Substrate();
		substrate.dryMatter = 10;
		substrate.organicDryMatter = 50;
		substrate.biogasProduction = 500;
		substrate.methaneContent = 50;

		var profile = new SubstrateProfile();
		profile.substrate = substrate;
		profile.hourlyValues = new double[Stats.HOURS];
		Arrays.fill(profile.hourlyValues, 8.0);

		var plant = new BiogasPlant();
		plant.boilers.add(plantBoiler);
		plant.substrateProfiles.add(profile);
		plant.gasStorageSize = storageSize;
		return plant;
	}
}
