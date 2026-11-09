package sophena.calc.biogas;

import org.openlca.commons.Res;

import sophena.calc.biogas.fermentersim.FermenterSimulation;
import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.model.biogas.BiogasPlant;

public class FermenterHeatDemand {

	private static final boolean FULL_SIM = true;

	private FermenterHeatDemand() {
	}

	public static double[] of(Project project, BiogasPlant plant) {
		var res = FULL_SIM
			? fromFullSimulation(project, plant)
			: fromSimpleInterpolation(project, plant);
		return res.isError()
			? new double[Stats.HOURS]
			: res.value();
	}

	private static Res<double[]> fromFullSimulation(
		Project project, BiogasPlant plant
	) {
		if (project == null)
			return Res.error("Es sind keine gültigen Wetterdaten vorhanden.");

		// the rated power depends on the run hours of the plant; when the
		// plant cannot be calculated, there is no fermenter heat demand
		var runtime = BiogasRuntimeResult.calculate(plant);
		if (runtime.isError())
			return runtime.castError();
		double ratedPower = runtime.value().ratedPower();
		return of(project.weatherStation, plant, ratedPower);
	}

	/// Calculates the hourly heat demand of the fermenters of the given plant
	/// in kW with the weather data of the given station. The rated power
	/// (Bemessungsleistung) of the plant in kW is only required when there is a
	/// fermenter with a double membrane roof (see `FermenterSimulation`).
	/// Returns an error when the plant has no fermenters or substrates or when
	/// the simulation fails.
	public static Res<double[]> of(
		WeatherStation station, BiogasPlant plant, double ratedPower
	) {
		var sim = FermenterSimulation.of(plant, station, ratedPower);
		if (sim.isError())
			return sim.castError();
		var res = sim.value().run();
		if (res.isError())
			return res.castError();
		return Res.ok(res.value().heatDemand());
	}

	private static Res<double[]> fromSimpleInterpolation(
		Project project, BiogasPlant plant
	) {
		if (project == null
			|| project.weatherStation == null
			|| project.weatherStation.data == null)
			return Res.error("Es sind keine gültigen Wetterdaten vorhanden.");


		if (plant == null || plant.boilers.isEmpty())
			return Res.error("Es wurde noch kein BHKW definiert.");
		double power = BiogasPlantCHP.of(plant).thermalPower();
		var demand = new double[Stats.HOURS];

		var tempData = project.weatherStation.data;
		double minTemp = Stats.min(tempData);
		double maxTemp = Stats.max(tempData);
		if (power < 0.1 || minTemp == maxTemp)
			return Res.ok(demand);

		for (int h = 0; h < tempData.length; h++) {
			var temp = tempData[h];
			var share = 1 - (temp - minTemp) / (maxTemp - minTemp);
			demand[h] = 0.2 * share * power;
		}
		return Res.ok(demand);
	}
}
