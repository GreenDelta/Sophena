package sophena.calc.biogas.fermentersim;

import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Res;

import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;

/**
 * Main simulation runner executing the quasi-stationary fermenter heat demand model.
 */
public final class FermenterSimulation {

	private final BiogasPlant plant;
	private final WeatherStation station;
	private final Fermenter f;

	/// Parameter name of the original fermenter
	/// simulation: `bhkwMeanElectricPowerKW`.
	private final double chpMeanPower;

	/// Parameter name of the original fermenter
	/// simulation: `bhkwElectricEfficiency`.
	private final double chpEfficiency;

	private FermenterSimulation(BiogasPlant plant, WeatherStation station) {
		this.plant = plant;
		this.station = station;
		this.f = plant.fermenter;
		this.chpMeanPower = Utils.meanElectricPowerOf(plant);
		this.chpEfficiency = Utils.electricEfficiencyOf(plant);
	}

	public static Res<FermenterSimulation> of(
		BiogasPlant plant, WeatherStation station
	) {
		var res = InputValidation.of(station);
		if (res.isError())
			return res.castError();

		res = InputValidation.of(plant);
		if (res.isError())
			return res.castError();

		var sim = new FermenterSimulation(plant, station);
		return Res.ok(sim);
	}

	public SimulationResult run() {

		var groundTemps = SoilTemperatureCalculator.calculate(station, f);
		var roofGeo = (f.roofType == RoofType.FIXED)
			? RoofGeometry.createFixed(f)
			: RoofGeometry.createDoubleMembrane(f);

		double fermenterVolumeM3 = Math.PI * (f.wallInnerRadius() * f.wallInnerRadius()) * f.wallTotalHeight;
		double mixerInstalledPowerKW = f.mixerPowerDensity * fermenterVolumeM3 / 1000.0;
		double mixerMeanHeatKW = mixerInstalledPowerKW * (f.mixerRuntime / 60.0) * f.mixerHeatFraction;
		double qMixerGainW = 1000.0 * mixerMeanHeatKW;

		double r1 = f.wallInnerRadius();
		double r2 = f.wallOuterRadius - f.wallInsulationThickness;
		double r3 = f.wallOuterRadius;
		double r4 = r3 + Const.soilBuffer;

		double rWallEarthGeometryMKW = Math.log(r2 / r1) / Const.wLambdaWmK
			+ Math.log(r3 / r2) / Const.wLambdaIWmK
			+ Math.log(r4 / r3) / Const.boLambdaGrWmK;

		double wallEarthHeightM = f.wallTotalHeight * f.wallBuriedFraction;
		double uaWallEarthWK = 2.0 * Math.PI * wallEarthHeightM / rWallEarthGeometryMKW;

		double aFloorM2 = Math.PI * r1 * r1;
		double rFloorM2KW = f.floorSlabThickness / Const.boLambdaWmK
			+ f.floorInsulationThickness / Const.boLambdaIWmK;

		List<SimulationResultStep> steps = new ArrayList<>(Stats.HOURS);

		double totalHeatKwSum = 0.0;
		double peakKw = 0.0;

		double[] prevMembraneTempsC = new double[3];

		for (int k = 0; k < Stats.HOURS; k++) {
			var stepInput = StepInput.of(plant, station, k);
			var solar = SolarCalculator.computeStepSolar(station, f, roofGeo, stepInput);

			if (k == 0) {
				prevMembraneTempsC[0] = f.targetTemperature - 1.0;
				prevMembraneTempsC[1] = 0.5 * (f.targetTemperature + stepInput.tAirC());
				prevMembraneTempsC[2] = stepInput.tAirC() + 1.0;
			}

			var roofEval = evaluateRoof(f, roofGeo, stepInput, solar, prevMembraneTempsC);

			if (f.roofType == RoofType.DOUBLE_MEMBRANE) {
				prevMembraneTempsC[0] = roofEval.tsInnerMembraneC();
				prevMembraneTempsC[1] = roofEval.tsSupportAirC();
				prevMembraneTempsC[2] = roofEval.tsRoofC();
			}

			var wallEval = CylinderWallSolver.solve(f, stepInput, solar.qSolarAbsWallWm2(), solar.lDownWm2());

			double qWallEarthW = (f.targetTemperature - groundTemps.tEarthWallC()[k]) * uaWallEarthWK;
			double qFloorW = (aFloorM2 / rFloorM2KW) * (f.targetTemperature - groundTemps.tEarthBottomC()[k]);
			double feedKgS = stepInput.feedKgH() / 3600.0;
			double qFeedW = feedKgS * Const.sCpJkgK * (f.targetTemperature - stepInput.feedTemperature());

			double qBalanceW = roofEval.qRoofW() + wallEval.qWallAirW() + qWallEarthW + qFloorW + qFeedW - qMixerGainW;
			double qHeatKW = Math.max(0.0, qBalanceW) / 1000.0;

			totalHeatKwSum += qHeatKW;
			peakKw = Math.max(peakKw, qHeatKW);

			steps.add(new SimulationResultStep(
				k,
				stepInput.tAirC(),
				qHeatKW,
				roofEval.qRoofW(),
				wallEval.qWallAirW(),
				qWallEarthW,
				qFloorW,
				qFeedW,
				qMixerGainW,
				roofEval.tsRoofC(),
				roofEval.tsInnerMembraneC(),
				roofEval.tsSupportAirC(),
				wallEval.tsWallC(),
				roofEval.qInnerConvectionW(),
				roofEval.qInnerRadiationW(),
				roofEval.qGapInnerConvectionW(),
				roofEval.qGapOuterConvectionW(),
				roofEval.qMembraneRadiationW(),
				roofEval.qSupportAirAdvectionW()
			));
		}

		double totalEnergyMWh = totalHeatKwSum / 1000.0;
		return new SimulationResult(steps, totalEnergyMWh, peakKw);
	}

	private record RoofEval(
		double tsRoofC, double tsInnerMembraneC, double tsSupportAirC,
		double qRoofW,
		double qInnerConvectionW, double qInnerRadiationW,
		double qGapInnerConvectionW,
		double qGapOuterConvectionW, double qMembraneRadiationW,
		double qSupportAirAdvectionW
	) {
	}

	private RoofEval evaluateRoof(
		Fermenter f, RoofGeometry roofGeo,
		StepInput in, SolarCalculator.StepSolar solar, double[] prevTempsC
	) {
		if (f.roofType == RoofType.FIXED) {
			var res = FixedRoofSolver.solve(f, roofGeo, in, solar.qSolarAbsRoofWm2(), solar.lDownWm2());
			return new RoofEval(res.tsRoofC(), 0.0, 0.0, res.qRoofW(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		} else {
			var res = DoubleMembraneRoofSolver.solve(f, roofGeo, chpMeanPower, chpEfficiency, in, solar.qSolarAbsRoofWm2(), solar.lDownWm2(), prevTempsC);
			return new RoofEval(
				res.tsRoofC(), res.tsInnerMembraneC(), res.tsSupportAirC(), res.qRoofW(),
				res.qInnerConvectionW(), res.qInnerRadiationW(), res.qGapInnerConvectionW(),
				res.qGapOuterConvectionW(), res.qMembraneRadiationW(), res.qSupportAirAdvectionW()
			);
		}
	}
}
