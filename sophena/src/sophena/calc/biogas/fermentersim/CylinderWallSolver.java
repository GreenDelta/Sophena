package sophena.calc.biogas.fermentersim;

/**
 * Solves iterative energy balance and heat transfer for cylinder wall exposed to air.
 */
final class CylinderWallSolver {

	record WallResult(double tsWallC, double qWallAirW) {
	}

	private CylinderWallSolver() {
	}

	static WallResult solve(
		FermenterParameters p,
		FermenterMaterials m,
		FermenterConstants c,
		double tAirC,
		double windMps,
		double qSolarAbsWallWm2,
		double lDownWm2
	) {
		double wallAirHeightM = p.wHTotalM() * (1.0 - p.buriedWallFraction());
		double r1 = p.wRim();
		double r2 = p.wRaM() - p.wIm();
		double r3 = p.wRaM();

		double aWallAirM2 = 2.0 * Math.PI * r3 * wallAirHeightM;
		if (aWallAirM2 <= 0.0) {
			return new WallResult(0.0, 0.0);
		}

		double rWallM2KW = r3 * (Math.log(r2 / r1) / m.wLambdaWmK() + Math.log(r3 / r2) / m.wLambdaIWmK());
		double lWallM = Math.PI * r3;

		double outsideAirPrandtl = m.uEtaPas() * m.uCpJkgK() / m.uLambdaWmK();
		double outsideAirKinematicViscosityM2s = m.uEtaPas() / m.uRhoKgm3();

		double wallReynolds = windMps * lWallM / outsideAirKinematicViscosityM2s;
		double wallNuLaminar = 0.664 * Math.pow(outsideAirPrandtl, 1.0 / 3.0) * Math.sqrt(wallReynolds);
		double wallNuTurbulent = 0.037 * Math.pow(wallReynolds, 0.8) * Math.pow(outsideAirPrandtl, 0.48) / (
			1.0 + 2.443 * Math.pow(wallReynolds, -0.1) * (Math.pow(outsideAirPrandtl, 2.0 / 3.0) - 1.0)
		);

		double hWallForcedWm2K = (0.3 + Math.sqrt(wallNuLaminar * wallNuLaminar + wallNuTurbulent * wallNuTurbulent))
			* m.uLambdaWmK() / lWallM;

		double tSkyK = Math.pow(lDownWm2 / c.sigma(), 0.25);
		double tGroundK = tAirC + c.k0C();

		double tsWallC = tAirC;

		for (int iter = 1; iter <= c.maxIterations(); iter++) {
			double wallGrashof = c.g() * Math.pow(wallAirHeightM, 3) * Math.abs(tAirC - tsWallC)
				/ ((tAirC + c.k0C()) * outsideAirKinematicViscosityM2s * outsideAirKinematicViscosityM2s);
			double wallRayleigh = wallGrashof * outsideAirPrandtl;

			double wallNuFree = Math.pow(
				0.825 + 0.387 * Math.pow(wallRayleigh, 1.0 / 6.0) / Math.pow(
					1.0 + Math.pow(0.492 / outsideAirPrandtl, 9.0 / 16.0), 8.0 / 27.0
				), 2
			) + 0.435 * wallAirHeightM / (2.0 * p.wRaM());

			double hWallFreeWm2K = wallNuFree * m.uLambdaWmK() / wallAirHeightM;
			double hWallMixedWm2K = Math.pow(Math.pow(hWallForcedWm2K, 3) + Math.pow(hWallFreeWm2K, 3), 1.0 / 3.0);

			double tsWallK = tsWallC + c.k0C();
			double fSkyWall = 0.5;
			double fGroundWall = 0.5;

			double hWallRadSkyWm2K = m.wEpsilon() * fSkyWall * c.sigma() * (tsWallK + tSkyK) * (tsWallK * tsWallK + tSkyK * tSkyK);
			double hWallRadGroundWm2K = m.wEpsilon() * fGroundWall * c.sigma() * (tsWallK + tGroundK) * (tsWallK * tsWallK + tGroundK * tGroundK);

			double tsWallRawC = (
				p.tSetC() / rWallM2KW
					+ hWallMixedWm2K * tAirC
					+ hWallRadSkyWm2K * (tSkyK - c.k0C())
					+ hWallRadGroundWm2K * tAirC
					+ qSolarAbsWallWm2
			) / (
				1.0 / rWallM2KW + hWallMixedWm2K + hWallRadSkyWm2K + hWallRadGroundWm2K
			);

			double tsWallNewC = (1.0 - c.relaxation()) * tsWallC + c.relaxation() * tsWallRawC;
			double tempChangeK = Math.abs(tsWallNewC - tsWallC);
			tsWallC = tsWallNewC;

			if (tempChangeK < c.toleranceK()) {
				break;
			}
		}

		double qWallWm2 = (p.tSetC() - tsWallC) / rWallM2KW;
		double qWallAirW = aWallAirM2 * qWallWm2;

		return new WallResult(tsWallC, qWallAirW);
	}
}
