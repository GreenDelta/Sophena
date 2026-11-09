package sophena.calc.biogas.fermentersim;

import java.util.ArrayList;
import java.util.List;

import org.openlca.commons.Res;

import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;

/// Main simulation runner executing the quasi-stationary fermenter heat demand
/// model. The heat demand of each fermenter of the plant is simulated
/// separately with the substrate flow and feed temperature of the fermenter in
/// the fermenter network (see `FermenterNetwork`). The heat demand of the plant
/// is the sum of the heat demands of its fermenters.
public final class FermenterSimulation {

	private final BiogasPlant plant;
	private final WeatherStation station;
	private final FermenterNetwork network;
	private final double ratedPowerKW;

	private FermenterSimulation(
		BiogasPlant plant, WeatherStation station, FermenterNetwork network,
		double ratedPowerKW
	) {
		this.plant = plant;
		this.station = station;
		this.network = network;
		this.ratedPowerKW = ratedPowerKW;
	}

	/// Creates a simulation for the given plant and weather station. The rated
	/// power (Bemessungsleistung) of the plant in kW is the mean electric power
	/// of the CHP over the year, see `BiogasRuntimeResult#ratedPower()`. It
	/// determines the mean biogas flow and with this the support air flow of a
	/// double membrane roof; it is only required when there is a fermenter
	/// with a double membrane roof.
	public static Res<FermenterSimulation> of(
		BiogasPlant plant, WeatherStation station, double ratedPowerKW
	) {
		var res = InputValidation.of(station);
		if (res.isError())
			return res.castError();

		res = InputValidation.of(plant);
		if (res.isError())
			return res.castError();

		res = InputValidation.ofRatedPower(plant, ratedPowerKW);
		if (res.isError())
			return res.castError();

		var network = FermenterNetwork.of(plant);
		if (network.isError())
			return network.castError();

		var sim = new FermenterSimulation(
			plant, station, network.value(), ratedPowerKW);
		return Res.ok(sim);
	}

	public Res<SimulationResult> run() {
		try {
			var result = internalRun();
			return Res.ok(result);
		} catch (Exception e) {
			return Res.error(
				"Es gab einen unerwarteten Fehler in der Fermentersimulation: "
					+ e.getMessage());
		}
	}

	private SimulationResult internalRun() {
		// the hourly input values of the plant; these are adapted to the
		// substrate flow of each fermenter
		var scale = WeatherScale.of(station);
		var inputs = new StepInput[Stats.HOURS];
		for (int k = 0; k < Stats.HOURS; k++) {
			inputs[k] = StepInput.of(plant, scale, k);
		}

		var results = new ArrayList<FermenterResult>();
		var demand = new double[Stats.HOURS];
		for (var node : network.nodes()) {
			var r = runFermenter(node, inputs);
			results.add(r);
			for (var step : r.steps()) {
				demand[step.hour()] += step.qHeatKW();
			}
		}

		double totalEnergyMWh = Stats.sum(demand) / 1000.0;
		double peakKw = Stats.max(demand);
		return new SimulationResult(results, demand, totalEnergyMWh, peakKw);
	}

	private FermenterResult runFermenter(
		FermenterNetwork.Node node, StepInput[] inputs
	) {
		var f = node.fermenter();
		double fermenterRatedPowerKW = ratedPowerKW * node.gasShare();
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
			var stepInput = feedOf(node, inputs[k]);
			var solar = SolarCalculator.computeStepSolar(station, f, roofGeo, stepInput);

			if (k == 0) {
				prevMembraneTempsC[0] = f.targetTemperature - 1.0;
				prevMembraneTempsC[1] = 0.5 * (f.targetTemperature + stepInput.tAirC());
				prevMembraneTempsC[2] = stepInput.tAirC() + 1.0;
			}

			var roofEval = evaluateRoof(
				f, fermenterRatedPowerKW, roofGeo, stepInput, solar, prevMembraneTempsC);

			if (f.roofType == RoofType.DOUBLE_MEMBRANE) {
				prevMembraneTempsC[0] = roofEval.tsInnerMembraneC();
				prevMembraneTempsC[1] = roofEval.tsSupportAirC();
				prevMembraneTempsC[2] = roofEval.tsRoofC();
			}

			var wallEval = CylinderWallSolver.solve(f, stepInput, solar.qSolarAbsWallWm2(), solar.lDownWm2());

			double qWallEarthW = (f.targetTemperature - groundTemps.tEarthWallC()[k]) * uaWallEarthWK;
			double qFloorW = (aFloorM2 / rFloorM2KW) * (f.targetTemperature - groundTemps.tEarthBottomC()[k]);
			double feedKgS = stepInput.feedKgH() / 3600.0;
			double qFeedW = feedKgS * stepInput.substrateHeatCapacity() * (f.targetTemperature - stepInput.feedTemperature());

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
		return new FermenterResult(f, steps, totalEnergyMWh, peakKw);
	}

	/// Adapts the hourly input of the plant to the substrate flow of the given
	/// fermenter.
	private static StepInput feedOf(FermenterNetwork.Node node, StepInput in) {
		double feedTemperature = node.isInput()
			? in.feedTemperature()
			: node.feedTemperature();
		return in.withFeed(in.feedKgH() * node.massShare(), feedTemperature);
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
		Fermenter f, double fermenterRatedPowerKW, RoofGeometry roofGeo,
		StepInput in, SolarCalculator.StepSolar solar, double[] prevTempsC
	) {
		if (f.roofType == RoofType.FIXED) {
			var res = FixedRoofSolver.solve(f, roofGeo, in, solar.qSolarAbsRoofWm2(), solar.lDownWm2());
			return new RoofEval(res.tsRoofC(), 0.0, 0.0, res.qRoofW(), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
		} else {
			var res = DoubleMembraneRoofSolver.solve(plant, f, fermenterRatedPowerKW, roofGeo, in, solar.qSolarAbsRoofWm2(), solar.lDownWm2(), prevTempsC);
			return new RoofEval(
				res.tsRoofC(), res.tsInnerMembraneC(), res.tsSupportAirC(), res.qRoofW(),
				res.qInnerConvectionW(), res.qInnerRadiationW(), res.qGapInnerConvectionW(),
				res.qGapOuterConvectionW(), res.qMembraneRadiationW(), res.qSupportAirAdvectionW()
			);
		}
	}
}
