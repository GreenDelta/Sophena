package sophena.utils;

import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.WoodAmountType;
import sophena.rcp.app.App;

public class Producers {

	/// Initializes the fuel specification of a producer (or producer profile)
	/// based on the fuel group of the product group of the producer.
	public static void initFuelSpec(Producer p, Project project) {
		if (p == null || project == null)
			return;

		var spec = new FuelSpec();
		p.fuelSpec = spec;

		var productGroup = productGroupOf(p);
		if (productGroup == null)
			return;
		var fuelGroup = productGroup.fuelGroup;
		if (fuelGroup == null)
			return;

		if (fuelGroup == FuelGroup.ELECTRICITY) {
			// set the electricity mix from the project settings
			var cs = project.costSettings;
			if (cs != null && cs.electricityMix != null) {
				spec.fuel = cs.electricityMix;
				return;
			}
		}

		// find a matching fuel from the database
		for (var fuel : App.getDb().getAll(Fuel.class)) {
			if (fuel.group != fuelGroup)
				continue;
			spec.fuel = fuel;
			if (fuel.isProtected)
				break;
		}
		if (fuelGroup == FuelGroup.WOOD) {
			spec.waterContent = 20d;
			spec.woodAmountType = WoodAmountType.CHIPS;
		}
	}

	private static ProductGroup productGroupOf(Producer p) {
		if (p == null) return null;
		if (p.productGroup != null) return p.productGroup;
		if (p.boiler != null) return p.boiler.group;
		if (p.biogasPlant != null) return p.biogasPlant.productGroup;
		return null;
	}

	public static void initCosts(Producer p) {
		if (p == null)
			return;
		var costs = new ProductCosts();
		p.costs = costs;
		if (!p.hasProfile()) {
			p.heatRecoveryCosts = new ProductCosts();
		}

		if (p.boiler != null) {
			ProductCosts.copy(p.boiler, costs);
			return;
		}

		if (p.heatPump != null) {
			ProductCosts.copy(p.heatPump, costs);
			return;
		}

		if (p.solarCollector != null) {
			ProductCosts.copy(p.solarCollector, costs);
			return;
		}

		ProductCosts.copy(p.productGroup, costs);
	}

	/// Set the type of produced electricity for producers that are co-generation
	/// plants.
	public static void initElectricity(Producer p, Project project) {
		if (p == null
			|| p.productGroup == null
			|| p.productGroup.type != ProductType.COGENERATION_PLANT)
			return;

		// take replaced electricity mix from settings if available
		if (project != null) {
			var cs = project.costSettings;
			if (cs != null && cs.replacedElectricityMix != null) {
				p.producedElectricity = cs.replacedElectricityMix;
				return;
			}
		}

		p.producedElectricity = App.getDb().getAll(Fuel.class).stream()
			.filter(e -> e.group == FuelGroup.ELECTRICITY)
			.findFirst()
			.orElse(null);
	}
}
