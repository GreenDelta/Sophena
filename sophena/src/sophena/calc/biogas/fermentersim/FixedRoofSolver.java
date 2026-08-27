package sophena.calc.biogas.fermentersim;

import sophena.model.biogas.Fermenter;

/**
 * Solves heat transfer across a single-layer/fixed roof.
 */
final class FixedRoofSolver {

	record FixedRoofResult(double tsRoofC, double qRoofW) {
	}

	FixedRoofSolver() {
	}

	static FixedRoofResult solve(
		Fermenter f,
		RoofGeometry roofGeo,
		double tAirC,
		double windMps,
		double qSolarAbsRoofWm2,
		double lDownWm2
	) {
		double rRoofM2KW = f.roofFixedLayerThickness / Const.dLambdaWmK
			+ f.roofInsulationThickness / Const.dLambdaIWmK;
		double outsideAirPrandtl = Const.uEtaPas * Const.uCpJkgK / Const.uLambdaWmK;
		double outsideAirKinematicViscosityM2s = Const.uEtaPas / Const.uRhoKgm3;

		double roofExternalReynolds = windMps * roofGeo.roofExternalFlowLengthM() / outsideAirKinematicViscosityM2s;
		double roofExternalNusselt = roofExternalNusselt(roofExternalReynolds, outsideAirPrandtl);

		double hRoofExternalWm2K = roofExternalNusselt * Const.uLambdaWmK / roofGeo.roofExternalFlowLengthM();

		double tSkyK = Math.pow(lDownWm2 / Const.sigma, 0.25);
		double tGroundK = tAirC + Const.k0C;

		double tsRoofC = tAirC;

		for (int iter = 1; iter <= Const.maxIterations; iter++) {
			double tsRoofK = tsRoofC + Const.k0C;
			double hRadSkyWm2K = Const.dEpsilonDA * roofGeo.fSkyRoof() * Const.sigma * (tsRoofK + tSkyK) * (tsRoofK * tsRoofK + tSkyK * tSkyK);
			double hRadGroundWm2K = Const.dEpsilonDA * roofGeo.fGroundRoof() * Const.sigma * (tsRoofK + tGroundK) * (tsRoofK * tsRoofK + tGroundK * tGroundK);

			double tsRoofRawC = (
				f.targetTemperature / rRoofM2KW
					+ hRoofExternalWm2K * tAirC
					+ hRadSkyWm2K * (tSkyK - Const.k0C)
					+ hRadGroundWm2K * tAirC
					+ qSolarAbsRoofWm2
			) / (
				1.0 / rRoofM2KW + hRoofExternalWm2K + hRadSkyWm2K + hRadGroundWm2K
			);

			double tsRoofNewC = (1.0 - Const.relaxation) * tsRoofC + Const.relaxation * tsRoofRawC;
			double tempChangeK = Math.abs(tsRoofNewC - tsRoofC);
			tsRoofC = tsRoofNewC;

			if (tempChangeK < Const.tolerance) {
				break;
			}
		}

		double qRoofWm2 = (f.targetTemperature - tsRoofC) / rRoofM2KW;
		double qRoofW = roofGeo.aRoofM2() * qRoofWm2;

		return new FixedRoofResult(tsRoofC, qRoofW);
	}

	private static double roofExternalNusselt(
		double roofExternalReynolds, double outsideAirPrandtl
	) {
		double roofExternalNuLaminar = 0.664 * Math.sqrt(roofExternalReynolds) * Math.pow(outsideAirPrandtl, 1.0 / 3.0);
		if (roofExternalReynolds < Const.reTransition) {
			return roofExternalNuLaminar;
		} else {
			double roofExternalNuTurbulent = 0.037 * Math.pow(roofExternalReynolds, 0.8) * outsideAirPrandtl / (
				1.0 + 2.443 * Math.pow(roofExternalReynolds, -0.1) * (Math.pow(outsideAirPrandtl, 2.0 / 3.0) - 1.0)
			);
			return Math.sqrt(roofExternalNuLaminar * roofExternalNuLaminar + roofExternalNuTurbulent * roofExternalNuTurbulent);
		}
	}
}
