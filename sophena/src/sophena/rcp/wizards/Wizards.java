package sophena.rcp.wizards;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.widgets.Combo;

import sophena.Labels;
import sophena.db.daos.FuelDao;
import sophena.model.Boiler;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductCosts;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.WoodAmountType;
import sophena.rcp.App;

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
		String[] items = new String[2];
		items[0] = Labels.get(ProducerFunction.BASE_LOAD);
		items[1] = Labels.get(ProducerFunction.PEAK_LOAD);
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
		else
			return ProducerFunction.PEAK_LOAD;
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

	/**
	 * Initializes the fuel specification of the producer (or producer profile)
	 * based on the fuel group in the product group of the producer.
	 */
	static void initFuelSpec(Producer p, Project project) {
		FuelSpec spec = new FuelSpec();
		p.fuelSpec = spec;
		if (p.productGroup == null)
			return;
		FuelGroup group = p.productGroup.fuelGroup;
		if (group == null)
			return;

		// set the electricity mix from project settings if applicable
		if (group == FuelGroup.ELECTRICITY && project != null) {
			CostSettings settings = project.costSettings;
			if (settings != null && settings.electricityMix != null) {
				spec.fuel = settings.electricityMix;
				return;
			}
		}

		// find a matching fuel from the base data
		FuelDao dao = new FuelDao(App.getDb());
		for (Fuel fuel : dao.getAll()) {
			if (fuel.group != group)
				continue;
			spec.fuel = fuel;
			if (fuel.isProtected)
				break;
		}
		if (group == FuelGroup.WOOD) {
			spec.waterContent = 20d;
			spec.woodAmountType = WoodAmountType.CHIPS;
		}
	}

	/**
	 * Set the type of produced electricity for producers that are co-generation
	 * plants.
	 */
	static void initElectricity(Producer p, Project project) {
		if (p == null || p.productGroup == null)
			return;
		if (p.productGroup.type != ProductType.COGENERATION_PLANT)
			return;

		// take replaced electricity mix from settings if available
		if (project != null) {
			CostSettings settings = project.costSettings;
			if (settings != null && settings.replacedElectricityMix != null) {
				p.producedElectricity = settings.replacedElectricityMix;
				return;
			}
		}

		p.producedElectricity = new FuelDao(App.getDb())
				.getAll().stream()
				.filter(e -> e.group == FuelGroup.ELECTRICITY)
				.findFirst().orElse(null);
	}

	static void initCosts(Producer p) {
		if (p == null)
			return;
		ProductCosts costs = new ProductCosts();
		p.costs = costs;
		Boiler b = p.boiler;
		if (b != null) {
			ProductCosts.copy(b, costs);
		} else {
			ProductCosts.copy(p.productGroup, costs);
		}
		if (!p.hasProfile()) {
			p.heatRecoveryCosts = new ProductCosts();
		}
	}
}
