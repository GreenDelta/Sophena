package sophena.math.energetic;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class WoodMassTest {

	@Test
	public void testForAmount() {
		double wm = WoodMass
				.ofWoodChips_m3(5)
				.woodDensity_kg_per_m3(379)
				.waterContent(0.2)
				.get_kg();
		assertEquals(947.5, wm, 1e-10);
	}

	@Test
	public void testForEnergy() {
		double wm = WoodMass
				.ofEnergy_kWh(1200)
				.waterContent(0.2)
				.calorificValue_kWh_per_kg(5)
				.get_kg();
		assertEquals(310.55900621118013, wm, 1e-10);
	}

}
