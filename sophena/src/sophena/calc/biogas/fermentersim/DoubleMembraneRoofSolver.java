package sophena.calc.biogas.fermentersim;

import sophena.model.biogas.Fermenter;

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
		Fermenter f,
		RoofGeometry roofGeo,
		double bhkwMeanElectricPowerKW,
		double bhkwElectricEfficiency,
		StepInput in,
		double qSolarAbsRoofWm2,
		double lDownWm2,
		double[] prevTempsC
	) {
		double tSkyK = Math.pow(lDownWm2 / Const.sigma, 0.25);
		double tSkyC = tSkyK - Const.k0C;
		double tAmbientK = in.tAirC() + Const.k0C;

		double biogasFlowNm3h = bhkwMeanElectricPowerKW / (
			bhkwElectricEfficiency * Const.methaneHeatingValue * in.methaneContent()
		);
		double supportAirExchangeFlowM3h = biogasFlowNm3h * (f.targetTemperature + Const.k0C) / Const.normalTemperatureK;
		double supportAirVelocity = 0.5 * supportAirExchangeFlowM3h / (3600.0 * roofGeo.channelAreaM2());
		double supportAirPrandtl = Const.uEtaPas * Const.uCpJkgK / Const.uLambdaWmK;

		double hGapInnerWK = computeGapConvection(supportAirVelocity, roofGeo.innerFlowLengthM(), supportAirPrandtl) * roofGeo.innerMembraneAreaM2();
		double hGapOuterWK = computeGapConvection(supportAirVelocity, roofGeo.outerFlowLengthM(), supportAirPrandtl) * roofGeo.aRoofM2();
		double hSupportAirAdvectionWK = Const.uRhoKgm3 * Const.uCpJkgK * supportAirExchangeFlowM3h / 3600.0;

		double outsideAirPrandtl = Const.uEtaPas * Const.uCpJkgK / Const.uLambdaWmK;
		double outsideAirKinematicViscosityM2s = Const.uEtaPas / Const.uRhoKgm3;

		double roofExternalReynolds = in.windMps() * roofGeo.roofExternalFlowLengthM() / outsideAirKinematicViscosityM2s;
		double roofExternalNusselt = roofExternalNusselt(roofExternalReynolds, outsideAirPrandtl);

		double hRoofExternalWm2K = roofExternalNusselt * Const.uLambdaWmK / roofGeo.roofExternalFlowLengthM();
		double hExternalConvectionWK = hRoofExternalWm2K * roofGeo.aRoofM2();
		double qSolarW = qSolarAbsRoofWm2 * roofGeo.aRoofM2();

		double rRadSubstrateInnerM2 = (1.0 - Const.liquidSurfaceEpsilon) / (Const.liquidSurfaceEpsilon * roofGeo.aRoofProjectedM2())
			+ 1.0 / roofGeo.aRoofProjectedM2()
			+ (1.0 - Const.innerMembraneEpsilonInterior) / (Const.innerMembraneEpsilonInterior * roofGeo.innerMembraneAreaM2());

		double rRadInnerOuterM2 = (1.0 - Const.innerMembraneEpsilonGap) / (Const.innerMembraneEpsilonGap * roofGeo.innerMembraneAreaM2())
			+ 1.0 / roofGeo.innerMembraneAreaM2()
			+ (1.0 - Const.outerMembraneEpsilonGap) / (Const.outerMembraneEpsilonGap * roofGeo.aRoofM2());

		double[] tempsC = prevTempsC.clone();
		double maxResidualW = Double.POSITIVE_INFINITY;
		boolean converged = false;

		for (int iter = 1; iter <= Const.maxIterations; iter++) {
			double tInnerK = tempsC[0] + Const.k0C;
			double tOuterK = tempsC[2] + Const.k0C;
			double tSubstrateK = f.targetTemperature + Const.k0C;

			var natConvection = NaturalConvectionHelper.compute(
				f.targetTemperature, tempsC[0], roofGeo.aRoofProjectedM2(), f.wallInnerRadius()
			);
			double hInnerConvectionWK = natConvection.hNatWm2K() * roofGeo.aRoofProjectedM2();

			double hRadSubstrateInnerWK = Const.sigma * (tSubstrateK + tInnerK) * (tSubstrateK * tSubstrateK + tInnerK * tInnerK) / rRadSubstrateInnerM2;
			double hRadInnerOuterWK = Const.sigma * (tInnerK + tOuterK) * (tInnerK * tInnerK + tOuterK * tOuterK) / rRadInnerOuterM2;
			double hRadSkyWK = Const.outerMembraneEpsilonExterior * roofGeo.fSkyRoof() * roofGeo.aRoofM2() * Const.sigma * (tOuterK + tSkyK) * (tOuterK * tOuterK + tSkyK * tSkyK);
			double hRadGroundWK = Const.outerMembraneEpsilonExterior * roofGeo.fGroundRoof() * roofGeo.aRoofM2() * Const.sigma * (tOuterK + tAmbientK) * (tOuterK * tOuterK + tAmbientK * tAmbientK);

			double hFromSubstrateWK = hInnerConvectionWK + hRadSubstrateInnerWK;

			double[][] matrix = new double[][]{
				{hFromSubstrateWK + hGapInnerWK + hRadInnerOuterWK, -hGapInnerWK, -hRadInnerOuterWK},
				{-hGapInnerWK, hGapInnerWK + hGapOuterWK + hSupportAirAdvectionWK, -hGapOuterWK},
				{-hRadInnerOuterWK, -hGapOuterWK, hRadInnerOuterWK + hGapOuterWK + hExternalConvectionWK + hRadSkyWK + hRadGroundWK}
			};

			double[] rhs = new double[]{
				hFromSubstrateWK * f.targetTemperature,
				hSupportAirAdvectionWK * in.tAirC(),
				(hExternalConvectionWK + hRadGroundWK) * in.tAirC() + hRadSkyWK * tSkyC + qSolarW
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
			throw new IllegalStateException("Double membrane solver did not converge at hour " + in.hour() + ", max residual: " + maxResidualW);
		}

		return computeFinalFluxes(f, roofGeo, in.hour(), in.tAirC(), tempsC, hGapInnerWK, hGapOuterWK, hSupportAirAdvectionWK, hExternalConvectionWK, qSolarW, rRadSubstrateInnerM2, rRadInnerOuterM2, tSkyK, tAmbientK);
	}

	private static double computeGapConvection(
		double velocity,
		double length,
		double prandtl
	) {
		double reynolds = Const.uRhoKgm3 * velocity * length / Const.uEtaPas;
		double nuLaminar = 0.664 * Math.sqrt(reynolds) * Math.pow(prandtl, 1.0 / 3.0);
		double nusselt;

		if (reynolds < Const.reTransition) {
			nusselt = nuLaminar;
		} else {
			double nuTurbulent = 0.037 * Math.pow(reynolds, 0.8) * prandtl / (
				1.0 + 2.443 * Math.pow(reynolds, -0.1) * (Math.pow(prandtl, 2.0 / 3.0) - 1.0)
			);
			nusselt = Math.sqrt(nuLaminar * nuLaminar + nuTurbulent * nuTurbulent);
		}

		return nusselt * Const.uLambdaWmK / length;
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
		Fermenter f,
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
		var natConvection = NaturalConvectionHelper.compute(f.targetTemperature, tempsC[0], roofGeo.aRoofProjectedM2(), f.wallInnerRadius());
		double hInnerConvectionWK = natConvection.hNatWm2K() * roofGeo.aRoofProjectedM2();

		double tSubstrateK = f.targetTemperature + Const.k0C;
		double tInnerK = tempsC[0] + Const.k0C;
		double tOuterK = tempsC[2] + Const.k0C;

		double qInnerConvectionW = hInnerConvectionWK * (f.targetTemperature - tempsC[0]);
		double qInnerRadiationW = Const.sigma * (Math.pow(tSubstrateK, 4) - Math.pow(tInnerK, 4)) / rRadSubstrateInnerM2;
		double qMembraneRadiationW = Const.sigma * (Math.pow(tInnerK, 4) - Math.pow(tOuterK, 4)) / rRadInnerOuterM2;
		double qGapInnerConvectionW = hGapInnerWK * (tempsC[0] - tempsC[1]);
		double qGapOuterConvectionW = hGapOuterWK * (tempsC[1] - tempsC[2]);
		double qSupportAirAdvectionW = hSupportAirAdvectionWK * (tempsC[1] - tAirC);

		double qOuterConvectionW = hExternalConvectionWK * (tempsC[2] - tAirC);
		double qOuterSkyRadiationW = Const.outerMembraneEpsilonExterior * roofGeo.fSkyRoof() * roofGeo.aRoofM2() * Const.sigma * (Math.pow(tOuterK, 4) - Math.pow(tSkyK, 4));
		double qOuterGroundRadiationW = Const.outerMembraneEpsilonExterior * roofGeo.fGroundRoof() * roofGeo.aRoofM2() * Const.sigma * (Math.pow(tOuterK, 4) - Math.pow(tAmbientK, 4));

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
