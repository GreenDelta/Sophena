package sophena.calc.biogas.fermentersim;

import sophena.model.biogas.RoofType;

/**
 * Solar radiation geometric decomposition and absorbed solar radiation computation.
 */
final class SolarCalculator {

	public record StepSolar(
		double qSolarAbsRoofWm2,
		double qSolarAbsWallWm2,
		double lDownWm2
	) {
	}

	private SolarCalculator() {
	}

	static StepSolar computeStepSolar(
		FermenterParameters p,
		FermenterMaterials m,
		FermenterConstants c,
		RoofGeometry roofGeo,
		int doy,
		double hod,
		double tAirC,
		double bHorWm2,
		double dHorWm2
	) {
		double dayAngleRad = Math.toRadians(360.0 / 365.0 * (doy - 81.0));
		double equationOfTimeMin = 9.87 * Math.sin(2.0 * dayAngleRad)
			- 7.53 * Math.cos(dayAngleRad)
			- 1.5 * Math.sin(dayAngleRad);

		var station = p.station();
		double solarTimeH = hod + (4.0 * (station.longitude - station.referenceLongitude) + equationOfTimeMin) / 60.0;
		double hourAngleDeg = 15.0 * (solarTimeH - 12.0);
		double declinationDeg = 23.45 * Utils.sind(360.0 / 365.0 * (284.0 + doy));

		double sinElevation = Utils.sind(station.latitude) * Utils.sind(declinationDeg)
			+ Utils.cosd(station.latitude) * Utils.cosd(declinationDeg) * Utils.cosd(hourAngleDeg);
		sinElevation = Math.clamp(sinElevation, -1.0, 1.0);
		double cosElevation = Math.sqrt(Math.max(0.0, 1.0 - sinElevation * sinElevation));

		double gHorWm2 = bHorWm2 + dHorWm2;
		double roofSolarFactor = 1.0 - p.roofShadingFraction();

		double roofAbsorptivity = (p.roofType() == RoofType.FIXED) ? m.dAlphaDA() : p.membraneRoofAlpha();

		double gRoofWm2 = roofSolarFactor * (
			bHorWm2 * roofGeo.aRoofProjectedM2() / roofGeo.aRoofM2()
				+ dHorWm2 * roofGeo.fSkyRoof()
				+ m.groundReflectance() * gHorWm2 * roofGeo.fGroundRoof()
		);

		double qSolarAbsRoofWm2 = roofAbsorptivity * gRoofWm2;
		double qSolarAbsWallWm2 = computeWallSolar(p, m, c, doy, bHorWm2, dHorWm2, gHorWm2, sinElevation, cosElevation);
		double lDownWm2 = c.skyEmissivity() * c.sigma() * Math.pow(tAirC + c.k0C(), 4);

		return new StepSolar(qSolarAbsRoofWm2, qSolarAbsWallWm2, lDownWm2);
	}

	private static double computeWallSolar(
		FermenterParameters p,
		FermenterMaterials m,
		FermenterConstants c,
		int doy,
		double bHorWm2,
		double dHorWm2,
		double gHorWm2,
		double sinElevation,
		double cosElevation
	) {
		double fSkyWall = 0.5;
		double fGroundWall = 0.5;

		double gOnWm2 = c.solarConstantWm2() * (1.0 + 0.033 * Utils.cosd(360.0 * doy / 365.0));
		double sinElevationPositive = Math.max(0.0, sinElevation);
		boolean sunAboveHorizon = sinElevationPositive > 0.0;

		double derivedDniWm2 = sunAboveHorizon ? (bHorWm2 / sinElevationPositive) : 0.0;
		double dniLimitedWm2 = Math.min(derivedDniWm2, gOnWm2);

		double bHorDirectUsedWm2 = dniLimitedWm2 * sinElevationPositive;
		double bHorReclassifiedDiffuseWm2 = Math.max(0.0, bHorWm2 - bHorDirectUsedWm2);

		double gWallDirectWm2 = sunAboveHorizon ? (dniLimitedWm2 * cosElevation / Math.PI) : 0.0;
		double gWallDiffuseWm2 = fSkyWall * (dHorWm2 + bHorReclassifiedDiffuseWm2);
		double gWallGroundWm2 = m.groundReflectance() * fGroundWall * gHorWm2;

		double wallSolarFactor = 1.0 - p.wallShadingFraction();
		return m.wAlphaWA() * wallSolarFactor * (gWallDirectWm2 + gWallDiffuseWm2 + gWallGroundWm2);
	}
}
