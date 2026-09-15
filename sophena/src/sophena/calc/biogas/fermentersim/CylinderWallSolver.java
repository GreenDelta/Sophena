package sophena.calc.biogas.fermentersim;

import sophena.model.biogas.Fermenter;

/**
 * Solves iterative energy balance and heat transfer for cylinder wall exposed to air.
 */
final class CylinderWallSolver {

	record WallResult(double tsWallC, double qWallAirW) {
	}

	private CylinderWallSolver() {
	}

	static WallResult solve(
		Fermenter f,
		StepInput in,
		double qSolarAbsWallWm2,
		double lDownWm2
	) {
		// Wall height exposed to the air
		double wallAirHeightM = f.wallTotalHeight * (1.0 - f.wallBuriedFraction);
		double r1 = f.wallInnerRadius();
		double r2 = f.wallOuterRadius - f.wallInsulationThickness;
		double r3 = f.wallOuterRadius;

		// External surface area of the wall exposed to air
		double aWallAirM2 = 2.0 * Math.PI * r3 * wallAirHeightM;
		if (aWallAirM2 <= 0.0) {
			return new WallResult(0.0, 0.0);
		}

		// Conductive thermal resistance of the cylinder wall (m2 K/W)
		double rWallM2KW = r3 * (Math.log(r2 / r1) / Const.wLambdaWmK + Math.log(r3 / r2) / Const.wLambdaIWmK);
		double lWallM = Math.PI * r3; // Characteristic length for external cross-flow

		double outsideAirPrandtl = Const.uEtaPas * Const.uCpJkgK / Const.uLambdaWmK;
		double outsideAirKinematicViscosityM2s = Const.uEtaPas / Const.uRhoKgm3;

		// Forced convection over the cylinder (Gnielinski / VDI Heat Atlas)
		double wallReynolds = in.windMps() * lWallM / outsideAirKinematicViscosityM2s;
		double wallNuLaminar = 0.664 * Math.pow(outsideAirPrandtl, 1.0 / 3.0) * Math.sqrt(wallReynolds);
		double wallNuTurbulent = 0.037 * Math.pow(wallReynolds, 0.8) * Math.pow(outsideAirPrandtl, 0.48) / (
			1.0 + 2.443 * Math.pow(wallReynolds, -0.1) * (Math.pow(outsideAirPrandtl, 2.0 / 3.0) - 1.0)
		);

		double hWallForcedWm2K = (0.3 + Math.sqrt(wallNuLaminar * wallNuLaminar + wallNuTurbulent * wallNuTurbulent))
			* Const.uLambdaWmK / lWallM;

		double tSkyK = Math.pow(lDownWm2 / Const.sigma, 0.25);
		double tGroundK = in.tAirC() + Const.k0C;

		// Initial guess for outer wall temperature
		double tsWallC = in.tAirC();

		// Successive linearisation loop for external surface energy balance
		for (int iter = 1; iter <= Const.maxIterations; iter++) {
			// Free convection parameters (Rayleigh, Grashof, Nusselt)
			double wallGrashof = Const.g * Math.pow(wallAirHeightM, 3) * Math.abs(in.tAirC() - tsWallC)
				/ ((in.tAirC() + Const.k0C) * outsideAirKinematicViscosityM2s * outsideAirKinematicViscosityM2s);
			double wallRayleigh = wallGrashof * outsideAirPrandtl;

			double wallNuFree = Math.pow(
				0.825 + 0.387 * Math.pow(wallRayleigh, 1.0 / 6.0) / Math.pow(
					1.0 + Math.pow(0.492 / outsideAirPrandtl, 9.0 / 16.0), 8.0 / 27.0
				), 2
			) + 0.435 * wallAirHeightM / (2.0 * f.wallOuterRadius);

			double hWallFreeWm2K = wallNuFree * Const.uLambdaWmK / wallAirHeightM;
			// Combined mixed convection coefficient (forced + free)
			double hWallMixedWm2K = Math.pow(Math.pow(hWallForcedWm2K, 3) + Math.pow(hWallFreeWm2K, 3), 1.0 / 3.0);

			double tsWallK = tsWallC + Const.k0C;
			double fSkyWall = 0.5;
			double fGroundWall = 0.5;

			// Linearized radiation heat transfer coefficients (Stefan-Boltzmann)
			double hWallRadSkyWm2K = Const.wEpsilon * fSkyWall * Const.sigma * (tsWallK + tSkyK) * (tsWallK * tsWallK + tSkyK * tSkyK);
			double hWallRadGroundWm2K = Const.wEpsilon * fGroundWall * Const.sigma * (tsWallK + tGroundK) * (tsWallK * tsWallK + tGroundK * tGroundK);

			// Analytical solution for the wall surface temperature from energy balance
			double tsWallRawC = (
				f.targetTemperature / rWallM2KW
					+ hWallMixedWm2K * in.tAirC()
					+ hWallRadSkyWm2K * (tSkyK - Const.k0C)
					+ hWallRadGroundWm2K * in.tAirC()
					+ qSolarAbsWallWm2
			) / (
				1.0 / rWallM2KW + hWallMixedWm2K + hWallRadSkyWm2K + hWallRadGroundWm2K
			);

			// Under-relaxation to stabilize convergence
			double tsWallNewC = (1.0 - Const.relaxation) * tsWallC + Const.relaxation * tsWallRawC;
			double tempChangeK = Math.abs(tsWallNewC - tsWallC);
			tsWallC = tsWallNewC;

			if (tempChangeK < Const.tolerance) {
				break;
			}
		}

		double qWallWm2 = (f.targetTemperature - tsWallC) / rWallM2KW;
		double qWallAirW = aWallAirM2 * qWallWm2;

		return new WallResult(tsWallC, qWallAirW);
	}
}
