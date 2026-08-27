package sophena.calc.biogas.fermentersim;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;

import sophena.model.Boiler;
import sophena.model.biogas.BiogasPlant;

final class Utils {

	private Utils() {
	}

	/// Sine of angle in degrees.
	static double sind(double deg) {
		return Math.sin(Math.toRadians(deg));
	}

	/// Cosine of angle in degrees.
	static double cosd(double deg) {
		return Math.cos(Math.toRadians(deg));
	}

	/// Solves `A x = b` for a given matrix `A` and vector `b`, then
	/// returns `x`
	static double[] solve(double[][] A, double[] b) {
		var matrix = new Array2DRowRealMatrix(A, false);
		var vector = new ArrayRealVector(b, false);
		return new LUDecomposition(matrix)
			.getSolver()
			.solve(vector)
			.toArray();
	}

	/// Mean electric power of the CHP unit in kW as the sum over all boiler
	/// blocks of the mean of their min and max electric power.
	static double meanElectricPowerOf(BiogasPlant plant) {
		double power = 0;
		for (var b : plant.boilers) {
			if (b == null || b.boiler == null)
				continue;
			power += meanBlockPower(b.boiler);
		}
		return power;
	}

	/// Electric efficiency of the CHP unit as the weighted average of the
	/// efficiency rates of all boiler blocks, weighted by their electric power.
	static double electricEfficiencyOf(BiogasPlant plant) {
		double weightedSum = 0;
		double totalPower = 0;
		for (var entry : plant.boilers) {
			if (entry == null || entry.boiler == null)
				continue;
			double power = meanBlockPower(entry.boiler);
			weightedSum += power * entry.boiler.efficiencyRateElectric;
			totalPower += power;
		}
		return totalPower > 0 ? weightedSum / totalPower : 0;
	}

	static double meanBlockPower(Boiler boiler) {
		return 0.5 * (boiler.maxPowerElectric + boiler.minPowerElectric);
	}
}
