package sophena.calc.load;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static sophena.calc.load.Smoothing.circularIndexOf;
import static sophena.calc.load.Smoothing.on;

import org.junit.Test;

import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Project;

public class SmoothingTest {

	@Test
	public void testSmoothingLength() {
		var project = new Project();
		var net = new HeatNet();
		project.heatNet = net;
		net.simultaneityFactor = 0.5;
		net.smoothingFactor = 20.0;
		assertEquals(6400.0, Smoothing.getCount(project), 1e-10);
	}

	@Test
	public void testCalculateFactor() {
		var project = new Project();
		project.heatNet = new HeatNet();
		project.heatNet.maxLoad = 524.0;
		project.heatNet.simultaneityFactor = 0.8;

		// add a customer so that the max. load
		// of the un-smoothed curve is 430
		var consumer = new Consumer();
		consumer.profile = LoadProfile.initEmpty();
		consumer.profile.dynamicData[0] = 430;
		project.consumers.add(consumer);

		assertEquals(3.5625, Smoothing.getFactor(project), 1e-4);
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
	public void testWindowLargerThanCurve() {
		// a window that covers the whole curve results in the mean
		var curve = new double[] { 1, 2, 3, 2, 1, 0 };
		assertArrayEquals(new double[] { 1.5, 1.5, 1.5, 1.5, 1.5, 1.5 },
				on(curve, 7), 1e-10);
		assertArrayEquals(new double[] { 1.5, 1.5, 1.5, 1.5, 1.5, 1.5 },
				on(curve, 204_800), 1e-10);
	}

	@Test
	public void testZerosStayZero() {
		var curve = new double[] { 0.1, 0.2, 0.3, 0, 0, 0, 0, 0, 0.7, 0.3 };
		var smoothed = on(curve, 3);
		assertEquals(0.1, smoothed[3], 1e-10);
		assertEquals(0.0, smoothed[4], 0.0);
		assertEquals(0.0, smoothed[5], 0.0);
		assertEquals(0.0, smoothed[6], 0.0);
	}

	@Test
	public void testCircularIndex() {
		// index in range(0, 8760)
		assertEquals(0, circularIndexOf(0, 8760));
		assertEquals(42, circularIndexOf(42, 8760));
		assertEquals(8759, circularIndexOf(8759, 8760));

		// positive overflow
		assertEquals(0, circularIndexOf(8760, 8760));
		assertEquals(1, circularIndexOf(8761, 8760));
		assertEquals(42, circularIndexOf(8802, 8760));
		assertEquals(8759, circularIndexOf(2 * 8760 - 1, 8760));
		assertEquals(0, circularIndexOf(2 * 8760, 8760));

		// negative overflow
		assertEquals(8759, circularIndexOf(-1, 8760));
		assertEquals(8718, circularIndexOf(-42, 8760));
		assertEquals(42, circularIndexOf(-8718, 8760));
		assertEquals(0, circularIndexOf(-8760, 8760));
		assertEquals(0, circularIndexOf(-2 * 8760, 8760));
	}

}
