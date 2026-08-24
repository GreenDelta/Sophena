package sophena.calc.biogas.fermentersim;

/**
 * Solves heat transfer across a single-layer/fixed roof.
 */
final class FixedRoofSolver {

	record FixedRoofResult(double tsRoofC, double qRoofW) {
	}

	FixedRoofSolver() {
	}

	static FixedRoofResult solve(
		FermenterParameters p,
		FermenterMaterials m,
		FermenterConstants c,
		RoofGeometry roofGeo,
		double tAirC,
		double windMps,
		double qSolarAbsRoofWm2,
		double lDownWm2
	) {
		double rRoofM2KW = p.dIm() / m.dLambdaWmK() + p.dSdM() / m.dLambdaIWmK();
		double outsideAirPrandtl = m.uEtaPas() * m.uCpJkgK() / m.uLambdaWmK();
		double outsideAirKinematicViscosityM2s = m.uEtaPas() / m.uRhoKgm3();

		double roofExternalReynolds = windMps * roofGeo.roofExternalFlowLengthM() / outsideAirKinematicViscosityM2s;
		double roofExternalNusselt = roofExternalNusselt(c, roofExternalReynolds, outsideAirPrandtl);

		double hRoofExternalWm2K = roofExternalNusselt * m.uLambdaWmK() / roofGeo.roofExternalFlowLengthM();

		double tSkyK = Math.pow(lDownWm2 / c.sigma(), 0.25);
		double tGroundK = tAirC + c.k0C();

		double tsRoofC = tAirC;

		for (int iter = 1; iter <= c.maxIterations(); iter++) {
			double tsRoofK = tsRoofC + c.k0C();
			double hRadSkyWm2K = m.dEpsilonDA() * roofGeo.fSkyRoof() * c.sigma() * (tsRoofK + tSkyK) * (tsRoofK * tsRoofK + tSkyK * tSkyK);
			double hRadGroundWm2K = m.dEpsilonDA() * roofGeo.fGroundRoof() * c.sigma() * (tsRoofK + tGroundK) * (tsRoofK * tsRoofK + tGroundK * tGroundK);

			double tsRoofRawC = (
				p.tSetC() / rRoofM2KW
					+ hRoofExternalWm2K * tAirC
					+ hRadSkyWm2K * (tSkyK - c.k0C())
					+ hRadGroundWm2K * tAirC
					+ qSolarAbsRoofWm2
			) / (
				1.0 / rRoofM2KW + hRoofExternalWm2K + hRadSkyWm2K + hRadGroundWm2K
			);

			double tsRoofNewC = (1.0 - c.relaxation()) * tsRoofC + c.relaxation() * tsRoofRawC;
			double tempChangeK = Math.abs(tsRoofNewC - tsRoofC);
			tsRoofC = tsRoofNewC;

			if (tempChangeK < c.toleranceK()) {
				break;
			}
		}

		double qRoofWm2 = (p.tSetC() - tsRoofC) / rRoofM2KW;
		double qRoofW = roofGeo.aRoofM2() * qRoofWm2;

		return new FixedRoofResult(tsRoofC, qRoofW);
	}

	private static double roofExternalNusselt(
		FermenterConstants c, double roofExternalReynolds, double outsideAirPrandtl
	) {
		double roofExternalNuLaminar = 0.664 * Math.sqrt(roofExternalReynolds) * Math.pow(outsideAirPrandtl, 1.0 / 3.0);
		if (roofExternalReynolds < c.reTransition()) {
			return roofExternalNuLaminar;
		} else {
			double roofExternalNuTurbulent = 0.037 * Math.pow(roofExternalReynolds, 0.8) * outsideAirPrandtl / (
				1.0 + 2.443 * Math.pow(roofExternalReynolds, -0.1) * (Math.pow(outsideAirPrandtl, 2.0 / 3.0) - 1.0)
			);
			return Math.sqrt(roofExternalNuLaminar * roofExternalNuLaminar + roofExternalNuTurbulent * roofExternalNuTurbulent);
		}
	}
}
