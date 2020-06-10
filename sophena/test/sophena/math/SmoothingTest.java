package sophena.math;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static sophena.math.Smoothing.circularIndex;
import static sophena.math.Smoothing.on;

import org.junit.Test;

import sophena.model.HeatNet;
import sophena.model.Project;

public class SmoothingTest {

	@Test
	public void testSmoothingLength() {
		var project = new Project();
		var heatnet = new HeatNet();
		project.heatNet = heatnet;
		heatnet.simultaneityFactor = 0.5;
		heatnet.smoothingFactor = 20.0;
		assertEquals(6400.0, Smoothing.getCount(project), 1e-10);
	}

	@Test
	public void testMeans() {
		assertArrayEquals(new double[] { 1.8, 1.8, 1.8, 1.8, 1.8 },
				on(new double[] { 1, 2, 3, 2, 1 }, 5), 1e-10);
		assertArrayEquals(new double[] { 1, 2, 3, 2, 1 },
				on(new double[] { 1, 2, 3, 2, 1 }, 1), 1e-10);
		assertArrayEquals(new double[] { 4, 4, 5, 4, 4 },
				on(new double[] { 3, 6, 3, 6, 3 }, 2), 1e-10);
	}

	@Test
	public void testCircularIndex() {
		// index in range(0, 8760)
		assertEquals(0, circularIndex(0, 8760));
		assertEquals(42, circularIndex(42, 8760));
		assertEquals(8759, circularIndex(8759, 8760));

		// positive overflow
		assertEquals(0, circularIndex(8760, 8760));
		assertEquals(1, circularIndex(8761, 8760));
		assertEquals(42, circularIndex(8802, 8760));
		assertEquals(8759, circularIndex(2 * 8760 - 1, 8760));
		assertEquals(0, circularIndex(2 * 8760, 8760));

		// negative overflow
		assertEquals(8759, circularIndex(-1, 8760));
		assertEquals(8718, circularIndex(-42, 8760));
		assertEquals(42, circularIndex(-8718, 8760));
		assertEquals(0, circularIndex(-8760, 8760));
		assertEquals(0, circularIndex(-2 * 8760, 8760));
	}

}
