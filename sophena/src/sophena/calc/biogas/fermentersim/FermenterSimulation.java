package sophena.calc.biogas.fermentersim;

import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Res;

import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.RoofType;

/**
 * Main simulation runner executing the quasi-stationary fermenter heat demand model.
 */
public final class FermenterSimulation {

	private final MaterialConstants mat = MaterialConstants.get();
	private final SimulationConstants sim = SimulationConstants.get();

	private final FermenterParameters p;
	private final SimulationInput input;

	private FermenterSimulation(FermenterParameters p, SimulationInput input) {
		this.p = p;
		this.input = input;
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

		var input = SimulationInput.constant(station, 3.5, 1875.0);
		var parameters = FermenterParameters.of(plant, station);
		var sim = new FermenterSimulation(parameters, input);
		return Res.ok(sim);
	}

	public SimulationResult run() {

		var f = p.fermenter();
		var groundTemps = SoilTemperatureCalculator.calculate(input, p, mat);
		var roofGeo = (f.roofType == RoofType.FIXED)
			? RoofGeometry.createFixed(p)
			: RoofGeometry.createDoubleMembrane(p, mat, sim);

		double fermenterVolumeM3 = Math.PI * (f.wallInnerRadius() * f.wallInnerRadius()) * f.wallTotalHeight;
		double mixerInstalledPowerKW = f.mixerPowerDensity * fermenterVolumeM3 / 1000.0;
		double mixerMeanHeatKW = mixerInstalledPowerKW * (p.fermenter().mixerRuntime / 60.0) * p.fermenter().mixerHeatFraction;
		double qMixerGainW = 1000.0 * mixerMeanHeatKW;

		double r1 = f.wallInnerRadius();
		double r2 = f.wallOuterRadius - f.wallInsulationThickness;
		double r3 = f.wallOuterRadius;
		double r4 = r3 + sim.soilBuffer();

		double rWallEarthGeometryMKW = Math.log(r2 / r1) / mat.wLambdaWmK()
			+ Math.log(r3 / r2) / mat.wLambdaIWmK()
			+ Math.log(r4 / r3) / mat.boLambdaGrWmK();

		double wallEarthHeightM = f.wallTotalHeight * f.wallBuriedFraction;
		double uaWallEarthWK = 2.0 * Math.PI * wallEarthHeightM / rWallEarthGeometryMKW;

		double aFloorM2 = Math.PI * r1 * r1;
		double rFloorM2KW = f.floorSlabThickness / mat.boLambdaWmK() + f.floorInsulationThickness / mat.boLambdaIWmK();

		int nSteps = input.size();
		List<SimulationResultStep> steps = new ArrayList<>(nSteps);

		double totalHeatKwSum = 0.0;
		double peakKw = 0.0;

		double[] prevMembraneTempsC = new double[3];

		for (int k = 0; k < nSteps; k++) {
			var stepInput = extractStepInput(input, k);
			var solar = SolarCalculator.computeStepSolar(
				p, mat, sim, roofGeo, stepInput.doy(), stepInput.hod(), stepInput.tAirC(), stepInput.bHorWm2(), stepInput.dHorWm2()
			);

			if (k == 0) {
				prevMembraneTempsC[0] = p.fermenter().targetTemperature - 1.0;
				prevMembraneTempsC[1] = 0.5 * (p.fermenter().targetTemperature + stepInput.tAirC());
				prevMembraneTempsC[2] = stepInput.tAirC() + 1.0;
			}

			var roofEval = evaluateRoof(p, roofGeo, k, stepInput, solar, prevMembraneTempsC);

			if (f.roofType == RoofType.DOUBLE_MEMBRANE) {
				prevMembraneTempsC[0] = roofEval.tsInnerMembraneC();
				prevMembraneTempsC[1] = roofEval.tsSupportAirC();
				prevMembraneTempsC[2] = roofEval.tsRoofC();
			}

			var wallEval = CylinderWallSolver.solve(p, mat, sim, stepInput.tAirC(), stepInput.windMps(), solar.qSolarAbsWallWm2(), solar.lDownWm2());

			double qWallEarthW = (p.fermenter().targetTemperature - groundTemps.tEarthWallC()[k]) * uaWallEarthWK;
			double qFloorW = (aFloorM2 / rFloorM2KW) * (p.fermenter().targetTemperature - groundTemps.tEarthBottomC()[k]);
			double feedKgS = stepInput.feedKgH() / 3600.0;
			double qFeedW = feedKgS * mat.sCpJkgK() * (p.fermenter().targetTemperature - stepInput.tAirC());

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

	private record StepInput(int doy, double hod, double tAirC, double bHorWm2,
	                         double dHorWm2, double windMps, double feedKgH) {
	}

	private StepInput extractStepInput(SimulationInput input, int k) {
		int doy = (k / 24) + 1;
		double hod = k % 24;
		var station = input.station();
		return new StepInput(
			doy, hod,
			station.data[k],
			station.directRadiation[k],
			station.diffuseRadiation[k],
			input.windMps(),
			input.feedKgH()[k]
		);
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
		FermenterParameters p, RoofGeometry roofGeo,
		int k, StepInput in, SolarCalculator.StepSolar solar, double[] prevTempsC
	) {
		if (p.fermenter().roofType == RoofType.FIXED) {
			var res = FixedRoofSolver.solve(p, mat, sim, roofGeo, in.tAirC(), in.windMps(), solar.qSolarAbsRoofWm2(), solar.lDownWm2());
			return new RoofEval(res.tsRoofC(), 0.0, 0.0, res.qRoofW(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		} else {
			var res = DoubleMembraneRoofSolver.solve(p, mat, sim, roofGeo, k, in.tAirC(), in.windMps(), solar.qSolarAbsRoofWm2(), solar.lDownWm2(), prevTempsC);
			return new RoofEval(
				res.tsRoofC(), res.tsInnerMembraneC(), res.tsSupportAirC(), res.qRoofW(),
				res.qInnerConvectionW(), res.qInnerRadiationW(), res.qGapInnerConvectionW(),
				res.qGapOuterConvectionW(), res.qMembraneRadiationW(), res.qSupportAirAdvectionW()
			);
		}
	}
}
