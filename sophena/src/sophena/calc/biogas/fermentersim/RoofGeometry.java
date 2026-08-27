package sophena.calc.biogas.fermentersim;

/**
 * Geometric parameters and flow lengths for fermenter roof models.
 */
record RoofGeometry(
	double aRoofM2,
	double aRoofProjectedM2,
	double innerMembraneAreaM2,
	double fSkyRoof,
	double fGroundRoof,
	double roofExternalFlowLengthM,
	double supportAirConvectionInnerWm2K,
	double supportAirConvectionOuterWm2K,
	double supportAirExchangeFlowScalarM3h
) {

	static RoofGeometry createFixed(FermenterParameters p) {
		double r1 = p.fermenter().wallInnerRadius();
		double aRoofProjectedM2 = Math.PI * r1 * r1;
		return new RoofGeometry(
			aRoofProjectedM2,
			aRoofProjectedM2,
			0.0,
			1.0,
			0.0,
			2.0 * r1,
			0.0,
			0.0,
			0.0
		);
	}

	static RoofGeometry createDoubleMembrane(
		FermenterParameters p,
		double bhkwMeanElectricPowerKW,
		double bhkwElectricEfficiency
	) {
		var f = p.fermenter();
		double r1 = f.wallInnerRadius();
		double aRoofProjectedM2 = Math.PI * r1 * r1;
		double aRoofM2 = Math.PI * (r1 * r1 + f.roofMembraneHeight * f.roofMembraneHeight);
		double meanInnerHeight = 0.5 * f.roofMembraneHeight;
		double innerMembraneAreaM2 = Math.PI * (r1 * r1 + meanInnerHeight * meanInnerHeight);

		double fSkyRoof = 0.5 * (1.0 + aRoofProjectedM2 / aRoofM2);
		double fGroundRoof = 1.0 - fSkyRoof;

		double biogasFlowNm3h = bhkwMeanElectricPowerKW / (
			bhkwElectricEfficiency * Const.methaneHeatingValue * p.biogasMethaneFraction()
		);
		double biogasFlowOperatingM3h = biogasFlowNm3h * (p.fermenter().targetTemperature + Const.k0C) / Const.normalTemperatureK;

		double outerRadius = (r1 * r1 + f.roofMembraneHeight * f.roofMembraneHeight) / (2.0 * f.roofMembraneHeight);
		double outerAngle = 4.0 * Math.atan(f.roofMembraneHeight / r1);
		double outerArea = 0.5 * (outerRadius * outerRadius) * (outerAngle - Math.sin(outerAngle));
		double outerLength = outerRadius * outerAngle;

		double innerRadius = (r1 * r1 + meanInnerHeight * meanInnerHeight) / (2.0 * meanInnerHeight);
		double innerAngle = 4.0 * Math.atan(meanInnerHeight / r1);
		double innerArea = 0.5 * (innerRadius * innerRadius) * (innerAngle - Math.sin(innerAngle));
		double innerLength = innerRadius * innerAngle;

		double channelArea = outerArea - innerArea;
		if (channelArea <= 0) {
			throw new IllegalArgumentException("Double membrane support air channel cross section must be > 0.");
		}

		double supportAirMeanFlow = 0.5 * biogasFlowOperatingM3h;
		double supportAirVelocity = supportAirMeanFlow / (3600.0 * channelArea);
		double supportAirPrandtl = Const.uEtaPas * Const.uCpJkgK / Const.uLambdaWmK;

		double hInner = computeGapConvection(supportAirVelocity, innerLength, supportAirPrandtl);
		double hOuter = computeGapConvection(supportAirVelocity, outerLength, supportAirPrandtl);

		return new RoofGeometry(
			aRoofM2,
			aRoofProjectedM2,
			innerMembraneAreaM2,
			fSkyRoof,
			fGroundRoof,
			outerLength,
			hInner,
			hOuter,
			biogasFlowOperatingM3h
		);
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
}
