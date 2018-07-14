package sophena.math;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static sophena.math.Smoothing.circularIndex;
import static sophena.math.Smoothing.means;

import org.junit.Test;

public class SmoothingTest {

	@Test
	public void testMeans() {
		assertArrayEquals(new double[] { 1.8, 1.8, 1.8, 1.8, 1.8 },
				means(new double[] { 1, 2, 3, 2, 1 }, 5), 1e-10);
		assertArrayEquals(new double[] { 1, 2, 3, 2, 1 },
				means(new double[] { 1, 2, 3, 2, 1 }, 1), 1e-10);
		assertArrayEquals(new double[] { 4, 4, 5, 4, 4 },
				means(new double[] { 3, 6, 3, 6, 3 }, 2), 1e-10);
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
