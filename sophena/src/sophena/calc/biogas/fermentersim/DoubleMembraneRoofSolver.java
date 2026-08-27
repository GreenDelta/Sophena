package sophena.calc.biogas.fermentersim;

/**
 * Solves coupled 3-node thermal energy balance for double membrane roofs.
 */
final class DoubleMembraneRoofSolver {

	record DoubleMembraneResult(
		double tsInnerMembraneC,
		double tsSupportAirC,
		double tsRoofC,
		double qRoofW,
		double qInnerConvectionW,
		double qInnerRadiationW,
		double qGapInnerConvectionW,
		double qGapOuterConvectionW,
		double qMembraneRadiationW,
		double qSupportAirAdvectionW
	) {
	}

	private DoubleMembraneRoofSolver() {
	}

	static DoubleMembraneResult solve(
		FermenterParameters p,
		MaterialConstants m,
		RoofGeometry roofGeo,
		int k,
		double tAirC,
		double windMps,
		double qSolarAbsRoofWm2,
		double lDownWm2,
		double[] prevTempsC
	) {
		double tSkyK = Math.pow(lDownWm2 / Const.sigma, 0.25);
		double tSkyC = tSkyK - Const.k0C;
		double tAmbientK = tAirC + Const.k0C;

		double hGapInnerWK = roofGeo.supportAirConvectionInnerWm2K() * roofGeo.innerMembraneAreaM2();
		double hGapOuterWK = roofGeo.supportAirConvectionOuterWm2K() * roofGeo.aRoofM2();
		double hSupportAirAdvectionWK = m.uRhoKgm3() * m.uCpJkgK() * roofGeo.supportAirExchangeFlowScalarM3h() / 3600.0;

		double outsideAirPrandtl = m.uEtaPas() * m.uCpJkgK() / m.uLambdaWmK();
		double outsideAirKinematicViscosityM2s = m.uEtaPas() / m.uRhoKgm3();

		double roofExternalReynolds = windMps * roofGeo.roofExternalFlowLengthM() / outsideAirKinematicViscosityM2s;
		double roofExternalNusselt = roofExternalNusselt(roofExternalReynolds, outsideAirPrandtl);

		double hRoofExternalWm2K = roofExternalNusselt * m.uLambdaWmK() / roofGeo.roofExternalFlowLengthM();
		double hExternalConvectionWK = hRoofExternalWm2K * roofGeo.aRoofM2();
		double qSolarW = qSolarAbsRoofWm2 * roofGeo.aRoofM2();

		double rRadSubstrateInnerM2 = (1.0 - p.liquidSurfaceEpsilon()) / (p.liquidSurfaceEpsilon() * roofGeo.aRoofProjectedM2())
			+ 1.0 / roofGeo.aRoofProjectedM2()
			+ (1.0 - p.innerMembraneEpsilonInterior()) / (p.innerMembraneEpsilonInterior() * roofGeo.innerMembraneAreaM2());

		double rRadInnerOuterM2 = (1.0 - p.innerMembraneEpsilonGap()) / (p.innerMembraneEpsilonGap() * roofGeo.innerMembraneAreaM2())
			+ 1.0 / roofGeo.innerMembraneAreaM2()
			+ (1.0 - p.outerMembraneEpsilonGap()) / (p.outerMembraneEpsilonGap() * roofGeo.aRoofM2());

		double[] tempsC = prevTempsC.clone();
		double maxResidualW = Double.POSITIVE_INFINITY;
		boolean converged = false;

		for (int iter = 1; iter <= Const.maxIterations; iter++) {
			double tInnerK = tempsC[0] + Const.k0C;
			double tOuterK = tempsC[2] + Const.k0C;
			double tSubstrateK = p.fermenter().targetTemperature + Const.k0C;

			var natConvection = NaturalConvectionHelper.compute(
				p.fermenter().targetTemperature, tempsC[0], roofGeo.aRoofProjectedM2(), p.fermenter().wallInnerRadius(), m
			);
			double hInnerConvectionWK = natConvection.hNatWm2K() * roofGeo.aRoofProjectedM2();

			double hRadSubstrateInnerWK = Const.sigma * (tSubstrateK + tInnerK) * (tSubstrateK * tSubstrateK + tInnerK * tInnerK) / rRadSubstrateInnerM2;
			double hRadInnerOuterWK = Const.sigma * (tInnerK + tOuterK) * (tInnerK * tInnerK + tOuterK * tOuterK) / rRadInnerOuterM2;
			double hRadSkyWK = p.outerMembraneEpsilonExterior() * roofGeo.fSkyRoof() * roofGeo.aRoofM2() * Const.sigma * (tOuterK + tSkyK) * (tOuterK * tOuterK + tSkyK * tSkyK);
			double hRadGroundWK = p.outerMembraneEpsilonExterior() * roofGeo.fGroundRoof() * roofGeo.aRoofM2() * Const.sigma * (tOuterK + tAmbientK) * (tOuterK * tOuterK + tAmbientK * tAmbientK);

			double hFromSubstrateWK = hInnerConvectionWK + hRadSubstrateInnerWK;

			double[][] matrix = new double[][]{
				{hFromSubstrateWK + hGapInnerWK + hRadInnerOuterWK, -hGapInnerWK, -hRadInnerOuterWK},
				{-hGapInnerWK, hGapInnerWK + hGapOuterWK + hSupportAirAdvectionWK, -hGapOuterWK},
				{-hRadInnerOuterWK, -hGapOuterWK, hRadInnerOuterWK + hGapOuterWK + hExternalConvectionWK + hRadSkyWK + hRadGroundWK}
			};

			double[] rhs = new double[]{
				hFromSubstrateWK * p.fermenter().targetTemperature,
				hSupportAirAdvectionWK * tAirC,
				(hExternalConvectionWK + hRadGroundWK) * tAirC + hRadSkyWK * tSkyC + qSolarW
			};

			double r0 = rhs[0] - (matrix[0][0] * tempsC[0] + matrix[0][1] * tempsC[1] + matrix[0][2] * tempsC[2]);
			double r1 = rhs[1] - (matrix[1][0] * tempsC[0] + matrix[1][1] * tempsC[1] + matrix[1][2] * tempsC[2]);
			double r2 = rhs[2] - (matrix[2][0] * tempsC[0] + matrix[2][1] * tempsC[1] + matrix[2][2] * tempsC[2]);
			maxResidualW = Math.max(Math.abs(r0), Math.max(Math.abs(r1), Math.abs(r2)));

			double[] tempsRawC = Utils.solve(matrix, rhs);
			double[] tempsNewC = new double[3];
			double maxTempChangeK = 0.0;
			for (int i = 0; i < 3; i++) {
				tempsNewC[i] = (1.0 - Const.relaxation) * tempsC[i] + Const.relaxation * tempsRawC[i];
				maxTempChangeK = Math.max(maxTempChangeK, Math.abs(tempsNewC[i] - tempsC[i]));
			}

			tempsC = tempsNewC;

			if (maxTempChangeK < Const.tolerance && maxResidualW < Const.residualTolerance) {
				converged = true;
				break;
			}
		}

		if (!converged) {
			throw new IllegalStateException("Double membrane solver did not converge at hour " + k + ", max residual: " + maxResidualW);
		}

		return computeFinalFluxes(p, m, roofGeo, k, tAirC, tempsC, hGapInnerWK, hGapOuterWK, hSupportAirAdvectionWK, hExternalConvectionWK, qSolarW, rRadSubstrateInnerM2, rRadInnerOuterM2, tSkyK, tAmbientK);
	}

	private static double roofExternalNusselt(double roofExternalReynolds, double outsideAirPrandtl) {
		double roofExternalNuLaminar = 0.664 * Math.sqrt(roofExternalReynolds) * Math.pow(outsideAirPrandtl, 1.0 / 3.0);
		double roofExternalNusselt;

		if (roofExternalReynolds < Const.reTransition) {
			roofExternalNusselt = roofExternalNuLaminar;
		} else {
			double roofExternalNuTurbulent = 0.037 * Math.pow(roofExternalReynolds, 0.8) * outsideAirPrandtl / (
				1.0 + 2.443 * Math.pow(roofExternalReynolds, -0.1) * (Math.pow(outsideAirPrandtl, 2.0 / 3.0) - 1.0)
			);
			roofExternalNusselt = Math.sqrt(roofExternalNuLaminar * roofExternalNuLaminar + roofExternalNuTurbulent * roofExternalNuTurbulent);
		}
		return roofExternalNusselt;
	}

	private static DoubleMembraneResult computeFinalFluxes(
		FermenterParameters p,
		MaterialConstants m,
		RoofGeometry roofGeo,
		int k,
		double tAirC,
		double[] tempsC,
		double hGapInnerWK,
		double hGapOuterWK,
		double hSupportAirAdvectionWK,
		double hExternalConvectionWK,
		double qSolarW,
		double rRadSubstrateInnerM2,
		double rRadInnerOuterM2,
		double tSkyK,
		double tAmbientK
	) {
		var natConvection = NaturalConvectionHelper.compute(p.fermenter().targetTemperature, tempsC[0], roofGeo.aRoofProjectedM2(), p.fermenter().wallInnerRadius(), m);
		double hInnerConvectionWK = natConvection.hNatWm2K() * roofGeo.aRoofProjectedM2();

		double tSubstrateK = p.fermenter().targetTemperature + Const.k0C;
		double tInnerK = tempsC[0] + Const.k0C;
		double tOuterK = tempsC[2] + Const.k0C;

		double qInnerConvectionW = hInnerConvectionWK * (p.fermenter().targetTemperature - tempsC[0]);
		double qInnerRadiationW = Const.sigma * (Math.pow(tSubstrateK, 4) - Math.pow(tInnerK, 4)) / rRadSubstrateInnerM2;
		double qMembraneRadiationW = Const.sigma * (Math.pow(tInnerK, 4) - Math.pow(tOuterK, 4)) / rRadInnerOuterM2;
		double qGapInnerConvectionW = hGapInnerWK * (tempsC[0] - tempsC[1]);
		double qGapOuterConvectionW = hGapOuterWK * (tempsC[1] - tempsC[2]);
		double qSupportAirAdvectionW = hSupportAirAdvectionWK * (tempsC[1] - tAirC);

		double qOuterConvectionW = hExternalConvectionWK * (tempsC[2] - tAirC);
		double qOuterSkyRadiationW = p.outerMembraneEpsilonExterior() * roofGeo.fSkyRoof() * roofGeo.aRoofM2() * Const.sigma * (Math.pow(tOuterK, 4) - Math.pow(tSkyK, 4));
		double qOuterGroundRadiationW = p.outerMembraneEpsilonExterior() * roofGeo.fGroundRoof() * roofGeo.aRoofM2() * Const.sigma * (Math.pow(tOuterK, 4) - Math.pow(tAmbientK, 4));

		double res0 = qInnerConvectionW + qInnerRadiationW - qGapInnerConvectionW - qMembraneRadiationW;
		double res1 = qGapInnerConvectionW - qGapOuterConvectionW - qSupportAirAdvectionW;
		double res2 = qGapOuterConvectionW + qMembraneRadiationW + qSolarW - qOuterConvectionW - qOuterSkyRadiationW - qOuterGroundRadiationW;
		double finalMaxRes = Math.max(Math.abs(res0), Math.max(Math.abs(res1), Math.abs(res2)));

		if (finalMaxRes >= Const.residualTolerance) {
			throw new IllegalStateException("Membrane energy balance not closed at hour " + k + ": max residual = " + finalMaxRes + " W");
		}

		double qRoofW = qInnerConvectionW + qInnerRadiationW;

		return new DoubleMembraneResult(
			tempsC[0],
			tempsC[1],
			tempsC[2],
			qRoofW,
			qInnerConvectionW,
			qInnerRadiationW,
			qGapInnerConvectionW,
			qGapOuterConvectionW,
			qMembraneRadiationW,
			qSupportAirAdvectionW
		);
	}
}
