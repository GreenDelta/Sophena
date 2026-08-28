package sophena.calc.biogas;

import org.openlca.commons.Res;

import sophena.calc.biogas.fermentersim.FermenterSimulation;
import sophena.model.Project;
import sophena.model.Stats;
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

		var sim = FermenterSimulation.of(plant, project.weatherStation);
		if (sim.isError())
			return sim.castError();
		var res = sim.value().run();
		if (res.isError())
			return res.castError();

		var demand = new double[Stats.HOURS];
		int i = 0;
		for (var step : res.value().steps()) {
			demand[i] = step.qHeatKW();
			i++;
			if (i >= Stats.HOURS)
				break;
		}

		return Res.ok(demand);
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
		double power = BiogasPlants.totalThermalPower(plant);
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
