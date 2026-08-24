package sophena.calc.biogas.fermentersim;

import java.util.ArrayList;
import java.util.List;

import sophena.model.biogas.RoofType;

/**
 * Main simulation runner executing the quasi-stationary fermenter heat demand model.
 */
public final class FermenterSimulation {

	/**
	 * Runs the simulation. The weather data are used directly from the weather
	 * station of the given input (read-only, never modified). The location for
	 * the solar calculations is taken from the weather station referenced by
	 * the parameters.
	 */
	public SimulationResult run(
		FermenterParameters p,
		FermenterMaterials m,
		FermenterConstants c,
		SimulationInput input
	) {
		if (p.station() == null) {
			throw new IllegalArgumentException(
				"FermenterParameters must reference a weather station for the location.");
		}
		var groundTemps = SoilTemperatureCalculator.calculate(input, p, m);
		var roofGeo = (p.roofType() == RoofType.FIXED)
			? RoofGeometry.createFixed(p)
			: RoofGeometry.createDoubleMembrane(p, m, c);

		double fermenterVolumeM3 = Math.PI * (p.wRim() * p.wRim()) * p.wHTotalM();
		double mixerInstalledPowerKW = p.mixerInstalledPowerDensityWm3() * fermenterVolumeM3 / 1000.0;
		double mixerMeanHeatKW = mixerInstalledPowerKW * (p.mixerRunTimeMinPerHour() / 60.0) * p.mixerHeatFraction();
		double qMixerGainW = 1000.0 * mixerMeanHeatKW;

		double r1 = p.wRim();
		double r2 = p.wRaM() - p.wIm();
		double r3 = p.wRaM();
		double r4 = r3 + c.soilBufferM();

		double rWallEarthGeometryMKW = Math.log(r2 / r1) / m.wLambdaWmK()
			+ Math.log(r3 / r2) / m.wLambdaIWmK()
			+ Math.log(r4 / r3) / m.boLambdaGrWmK();

		double wallEarthHeightM = p.wHTotalM() * p.buriedWallFraction();
		double uaWallEarthWK = 2.0 * Math.PI * wallEarthHeightM / rWallEarthGeometryMKW;

		double aFloorM2 = Math.PI * r1 * r1;
		double rFloorM2KW = p.boSM() / m.boLambdaWmK() + p.boIm() / m.boLambdaIWmK();

		int nSteps = input.size();
		List<SimulationResultStep> steps = new ArrayList<>(nSteps);

		double totalHeatKwSum = 0.0;
		double peakKw = 0.0;

		double[] prevMembraneTempsC = new double[3];

		for (int k = 0; k < nSteps; k++) {
			var stepInput = extractStepInput(input, k);
			var solar = SolarCalculator.computeStepSolar(
				p, m, c, roofGeo, stepInput.doy(), stepInput.hod(), stepInput.tAirC(), stepInput.bHorWm2(), stepInput.dHorWm2()
			);

			if (k == 0) {
				prevMembraneTempsC[0] = p.tSetC() - 1.0;
				prevMembraneTempsC[1] = 0.5 * (p.tSetC() + stepInput.tAirC());
				prevMembraneTempsC[2] = stepInput.tAirC() + 1.0;
			}

			var roofEval = evaluateRoof(p, m, c, roofGeo, k, stepInput, solar, prevMembraneTempsC);

			if (p.roofType() == RoofType.DOUBLE_MEMBRANE) {
				prevMembraneTempsC[0] = roofEval.tsInnerMembraneC();
				prevMembraneTempsC[1] = roofEval.tsSupportAirC();
				prevMembraneTempsC[2] = roofEval.tsRoofC();
			}

			var wallEval = CylinderWallSolver.solve(p, m, c, stepInput.tAirC(), stepInput.windMps(), solar.qSolarAbsWallWm2(), solar.lDownWm2());

			double qWallEarthW = (p.tSetC() - groundTemps.tEarthWallC()[k]) * uaWallEarthWK;
			double qFloorW = (aFloorM2 / rFloorM2KW) * (p.tSetC() - groundTemps.tEarthBottomC()[k]);
			double feedKgS = stepInput.feedKgH() / 3600.0;
			double qFeedW = feedKgS * m.sCpJkgK() * (p.tSetC() - stepInput.tAirC());

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
		return new SimulationResult(steps, totalEnergyMWh, peakKw, p, m, c);
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
		FermenterParameters p, FermenterMaterials m, FermenterConstants c, RoofGeometry roofGeo,
		int k, StepInput in, SolarCalculator.StepSolar solar, double[] prevTempsC
	) {
		if (p.roofType() == RoofType.FIXED) {
			var res = FixedRoofSolver.solve(p, m, c, roofGeo, in.tAirC(), in.windMps(), solar.qSolarAbsRoofWm2(), solar.lDownWm2());
			return new RoofEval(res.tsRoofC(), 0.0, 0.0, res.qRoofW(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		} else {
			var res = DoubleMembraneRoofSolver.solve(p, m, c, roofGeo, k, in.tAirC(), in.windMps(), solar.qSolarAbsRoofWm2(), solar.lDownWm2(), prevTempsC);
			return new RoofEval(
				res.tsRoofC(), res.tsInnerMembraneC(), res.tsSupportAirC(), res.qRoofW(),
				res.qInnerConvectionW(), res.qInnerRadiationW(), res.qGapInnerConvectionW(),
				res.qGapOuterConvectionW(), res.qMembraneRadiationW(), res.qSupportAirAdvectionW()
			);
		}
	}
}
