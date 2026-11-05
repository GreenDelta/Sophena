package sophena.calc.biogas.fermentersim;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.LUDecomposition;

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

	/// Electric efficiency of the CHP unit as the weighted average of the
	/// efficiency rates of all boiler blocks, weighted by their electric power.
	static double electricEfficiencyOf(BiogasPlant plant) {
		double weightedSum = 0;
		double totalPower = 0;
		for (var entry : plant.boilers) {
			if (entry == null || entry.boiler == null)
				continue;
			var boiler = entry.boiler;
			double power = 0.5 * (boiler.maxPowerElectric + boiler.minPowerElectric);
			weightedSum += power * boiler.efficiencyRateElectric;
			totalPower += power;
		}
		return totalPower > 0 ? weightedSum / totalPower : 0;
	}
}
