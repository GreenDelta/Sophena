package sophena.rcp.wizards;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Combo;

import sophena.Labels;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.SolarCollectorOperatingMode;
import sophena.model.SolarCollectorSpec;

/**
 * Some utility functions for the wizards.
 */
class Wizards {

	private Wizards() {
	}

	static int nextProducerRank(Project project) {
		if (project == null)
			return 1;
		Set<Integer> set = new HashSet<>();
		for (Producer p : project.producers) {
			set.add(p.rank);
		}
		int next = 1;
		while (set.contains(next)) {
			next++;
		}
		return next;
	}

	static void fillProducerFunctions(Project project, Combo combo) {
		if (project == null || combo == null)
			return;
		String[] items = new String[3];
		items[0] = Labels.get(ProducerFunction.BASE_LOAD);
		items[1] = Labels.get(ProducerFunction.PEAK_LOAD);
		items[2] = Labels.get(ProducerFunction.MAX_LOAD);
		int selection = 0;
		for (Producer p : project.producers) {
			if (p.function == ProducerFunction.BASE_LOAD) {
				selection = 1;
				break;
			}
		}
		combo.setItems(items);
		combo.select(selection);
	}

	static ProducerFunction getProducerFunction(Combo combo) {
		if (combo == null)
			return ProducerFunction.BASE_LOAD;
		int i = combo.getSelectionIndex();
		if (i == 0)
			return ProducerFunction.BASE_LOAD;
		else if (i==1)
			return ProducerFunction.PEAK_LOAD;
		else
			return ProducerFunction.MAX_LOAD;
	}

	static boolean producerRankExists(Project project, int rank) {
		if (project == null)
			return false;
		for (Producer p : project.producers) {
			if (p.rank == rank)
				return true;
		}
		return false;
	}

	static void initSolarCollectorSpec(Producer p) {
		if (p == null || p.productGroup == null)
			return;
		if (p.productGroup.type != ProductType.SOLAR_THERMAL_PLANT)
			return;

		SolarCollectorSpec solarCollectorSpec = new SolarCollectorSpec();
		solarCollectorSpec.solarCollectorArea = p.solarCollector.collectorArea;
		solarCollectorSpec.solarCollectorOperatingMode = SolarCollectorOperatingMode.AUTO_RADIATION;
		solarCollectorSpec.solarCollectorTilt = 35;
		solarCollectorSpec.solarCollectorTemperatureDifference = 5;
		solarCollectorSpec.solarCollectorTemperatureIncrease = 5;
		solarCollectorSpec.solarCollectorRadiationLimit = 400;
		p.solarCollectorSpec = solarCollectorSpec;
	}

}
