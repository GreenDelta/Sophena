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
				.get_t();
		assertEquals(0.9475, wm, 1e-10);
	}

}
