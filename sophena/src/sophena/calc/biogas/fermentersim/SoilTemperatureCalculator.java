package sophena.calc.biogas.fermentersim;

/**
 * Calculates earth temperatures using harmonic fit and Kusuda-Achenbach soil model.
 */
final class SoilTemperatureCalculator {

	public record GroundTemperatures(
		double[] tEarthWallC,
		double[] tEarthBottomC
	) {
	}

	private SoilTemperatureCalculator() {
	}

	static GroundTemperatures calculate(
		SimulationInput input,
		FermenterParameters p,
		FermenterMaterials m
	) {
		int nSteps = input.size();
		double depthBottom = p.wHTotalM() * p.buriedWallFraction();
		double annualPeriodS = 365.2425 * 24.0 * 3600.0;

		double[] annualPhaseRad = new double[nSteps];
		for (int k = 0; k < nSteps; k++) {
			annualPhaseRad[k] = 2.0 * Math.PI * (k * 3600.0) / annualPeriodS;
		}

		double[] annualCoeffs = fitAnnualHarmonics(annualPhaseRad, input.tAirC());

		double penetrationDepthM = Math.sqrt(m.soilThermalDiffusivityM2s() * annualPeriodS / Math.PI);
		double depthWall = 0.5 * depthBottom;

		double attWall = Math.exp(-depthWall / penetrationDepthM);
		double attBottom = Math.exp(-depthBottom / penetrationDepthM);

		double[] tEarthWallC = new double[nSteps];
		double[] tEarthBottomC = new double[nSteps];

		for (int k = 0; k < nSteps; k++) {
			double phaseW = annualPhaseRad[k] - depthWall / penetrationDepthM;
			double phaseB = annualPhaseRad[k] - depthBottom / penetrationDepthM;

			tEarthWallC[k] = annualCoeffs[0] + attWall * (annualCoeffs[1] * Math.cos(phaseW) + annualCoeffs[2] * Math.sin(phaseW));
			tEarthBottomC[k] = annualCoeffs[0] + attBottom * (annualCoeffs[1] * Math.cos(phaseB) + annualCoeffs[2] * Math.sin(phaseB));
		}

		return new GroundTemperatures(tEarthWallC, tEarthBottomC);
	}

	private static double[] fitAnnualHarmonics(double[] phaseRad, double[] tAirC) {
		double[][] xtx = new double[3][3];
		double[] xty = new double[3];

		int n = phaseRad.length;
		for (int i = 0; i < n; i++) {
			double c = Math.cos(phaseRad[i]);
			double s = Math.sin(phaseRad[i]);
			double y = tAirC[i];

			xtx[0][0] += 1.0;
			xtx[0][1] += c;
			xtx[0][2] += s;

			xtx[1][0] += c;
			xtx[1][1] += c * c;
			xtx[1][2] += c * s;

			xtx[2][0] += s;
			xtx[2][1] += c * s;
			xtx[2][2] += s * s;

			xty[0] += y;
			xty[1] += y * c;
			xty[2] += y * s;
		}

		return Utils.solve(xtx, xty);
	}
}
