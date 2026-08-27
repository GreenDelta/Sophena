package sophena.calc.biogas.fermentersim;

import sophena.model.WeatherStation;
import sophena.model.biogas.Fermenter;
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
		WeatherStation station, Fermenter f, RoofGeometry roofGeo, StepInput in
	) {
		double dayAngleRad = Math.toRadians(360.0 / 365.0 * (in.doy() - 81.0));
		double equationOfTimeMin = 9.87 * Math.sin(2.0 * dayAngleRad)
			- 7.53 * Math.cos(dayAngleRad)
			- 1.5 * Math.sin(dayAngleRad);

		double solarTimeH = in.hod() + (4.0 * (station.longitude - station.referenceLongitude) + equationOfTimeMin) / 60.0;
		double hourAngleDeg = 15.0 * (solarTimeH - 12.0);
		double declinationDeg = 23.45 * Utils.sind(360.0 / 365.0 * (284.0 + in.doy()));

		double sinElevation = Utils.sind(station.latitude) * Utils.sind(declinationDeg)
			+ Utils.cosd(station.latitude) * Utils.cosd(declinationDeg) * Utils.cosd(hourAngleDeg);
		sinElevation = Math.clamp(sinElevation, -1.0, 1.0);
		double cosElevation = Math.sqrt(Math.max(0.0, 1.0 - sinElevation * sinElevation));

		double gHorWm2 = in.bHorWm2() + in.dHorWm2();
		double roofSolarFactor = 1.0 - f.roofShadingFraction;

		double roofAbsorptivity = (f.roofType == RoofType.FIXED)
			? Const.dAlphaDA
			: Const.membraneRoofAlpha;

		double gRoofWm2 = roofSolarFactor * (
			in.bHorWm2() * roofGeo.aRoofProjectedM2() / roofGeo.aRoofM2()
				+ in.dHorWm2() * roofGeo.fSkyRoof()
				+ Const.groundReflectance * gHorWm2 * roofGeo.fGroundRoof()
		);

		double qSolarAbsRoofWm2 = roofAbsorptivity * gRoofWm2;
		double qSolarAbsWallWm2 = computeWallSolar(f, in, gHorWm2, sinElevation, cosElevation);
		double lDownWm2 = Const.skyEmissivity * Const.sigma * Math.pow(in.tAirC() + Const.k0C, 4);

		return new StepSolar(qSolarAbsRoofWm2, qSolarAbsWallWm2, lDownWm2);
	}

	private static double computeWallSolar(
		Fermenter f, StepInput in, double gHorWm2, double sinElevation, double cosElevation
	) {
		double fSkyWall = 0.5;
		double fGroundWall = 0.5;

		double gOnWm2 = Const.solarConstant * (1.0 + 0.033 * Utils.cosd(360.0 * in.doy() / 365.0));
		double sinElevationPositive = Math.max(0.0, sinElevation);
		boolean sunAboveHorizon = sinElevationPositive > 0.0;

		double derivedDniWm2 = sunAboveHorizon ? (in.bHorWm2() / sinElevationPositive) : 0.0;
		double dniLimitedWm2 = Math.min(derivedDniWm2, gOnWm2);

		double bHorDirectUsedWm2 = dniLimitedWm2 * sinElevationPositive;
		double bHorReclassifiedDiffuseWm2 = Math.max(0.0, in.bHorWm2() - bHorDirectUsedWm2);

		double gWallDirectWm2 = sunAboveHorizon ? (dniLimitedWm2 * cosElevation / Math.PI) : 0.0;
		double gWallDiffuseWm2 = fSkyWall * (in.dHorWm2() + bHorReclassifiedDiffuseWm2);
		double gWallGroundWm2 = Const.groundReflectance * fGroundWall * gHorWm2;

		double wallSolarFactor = 1.0 - f.wallShadingFraction;
		return Const.wAlphaWA * wallSolarFactor * (gWallDirectWm2 + gWallDiffuseWm2 + gWallGroundWm2);
	}
}
