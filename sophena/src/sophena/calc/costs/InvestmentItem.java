package sophena.calc.costs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Strings;

import sophena.Labels;
import sophena.calc.specs.SolarModules;
import sophena.model.AbstractProduct;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasPlant;

/// A common structure for investment data that is the starting point in
/// cost calculations. The different model classes are mapped into this
/// structure.
///
/// @param asset The name of the underlying asset, e.g. the product name.
/// @param productType The product type of the asset.
/// @param producer In case the asset describes a producer, it is
///     referenced in this field, for the calculation of demand-based costs.
/// @param initialInvestment The initial costs of the investment in EUR;
///     these can be lower than the full investment costs in case of a
///     refurbishment or general overhaul of an existing asset.
/// @param investment The total costs of the investment in EUR; while the
///     initial costs could be different, the total costs should be always
///     used when replacements, maintenance or repair costs are calculated.
/// @param duration The usage duration of the asset in years.
/// @param repair The fraction [%] of the total investment costs that is
///     used for repair.
/// @param maintenance The fraction [%] of the total investment costs that
///     is used for maintenance.
/// @param operation The hours per year that are used for the operation of
///     the asset.
public record InvestmentItem(
	String asset,
	ProductType productType,
	@Nullable Producer producer,
	double initialInvestment,
	double investment,
	int duration,
	double repair,
	double maintenance,
	double operation
) {

	@NonNull
	public static List<InvestmentItem> allOf(Project project) {
		if (project == null)
			return Collections.emptyList();

		var items = new ArrayList<InvestmentItem>();

		// producers & heat recoveries
		for (var p : project.producers) {
			if (p.disabled)
				continue;
			add(items, producerItemOf(p));
			add(items, itemOf(
				p.heatRecovery,
				p.heatRecoveryCosts,
				ProductType.HEAT_RECOVERY));
		}

		// flue gas cleanings
		for (var e : project.flueGasCleaningEntries) {
			add(items, itemOf(
				e.product,
				e.costs,
				ProductType.FLUE_GAS_CLEANING));
		}

		// buffer tank and pipes
		if (project.heatNet != null) {
			add(items, itemOf(
				project.heatNet.bufferTank,
				project.heatNet.bufferTankCosts,
				ProductType.BUFFER_TANK));

			for (var p : project.heatNet.pipes) {
				add(items, itemOf(p.pipe, p.costs, ProductType.PIPE));
			}
		}

		// transfer stations of consumers
		for (var c : project.consumers) {
			if (c.disabled)
				continue;
			add(items, itemOf(
				c.transferStation,
				c.transferStationCosts,
				ProductType.TRANSFER_STATION));
		}

		// generic product entries
		for (var e : project.productEntries) {
			add(items, itemOf(e.product, e.costs));
		}

		return items;
	}

	public static List<InvestmentItem> allOf(BiogasPlant plant) {
		if (plant == null)
			return Collections.emptyList();
		var items = new ArrayList<InvestmentItem>();
		for (var e : plant.investments) {
			add(items, itemOf(e));
		}
		for (var b : plant.boilers) {
			if (b == null)
				continue;
			add(items, itemOf(b.boiler, b.costs));
		}
		return items;
	}


	private static void add(List<InvestmentItem> items, InvestmentItem item) {
		if (item != null) {
			items.add(item);
		}
	}

	@Nullable
	private static InvestmentItem itemOf(BiogasInvestmentEntry entry) {
		if (entry == null || entry.costs == null)
			return null;
		String asset = entry.name;
		var type = ProductType.OTHER_EQUIPMENT;
		var group = entry.productGroup;
		if (group != null) {
			type = group.type;
			if (asset == null) {
				asset = group.name;
			}
		}

		var costs = entry.costs;
		var initial = entry.refurbishmentShare != null
			? costs.investment * (entry.refurbishmentShare / 100)
			: costs.investment;
		return new InvestmentItem(
			asset, type, null,
			initial,
			costs.investment,
			costs.duration,
			costs.repair,
			costs.maintenance,
			costs.operation
		);
	}


	@Nullable
	private static InvestmentItem producerItemOf(Producer producer) {
		if (producer == null)
			return null;

		var asset = producer.boiler != null
			? producer.boiler.name
			: producer.name;
		var type = typeOf(producer);

		var costs = producer.costs;
		if (ProductCosts.isEmpty(costs))
			return new InvestmentItem(
				asset, type, producer, 0, 0, 0, 0, 0, 0);

		var investment = costs.investment;
		if (producer.solarCollector != null && producer.solarCollectorSpec != null) {
			investment *= SolarModules.getCount(
				producer.solarCollectorSpec.solarCollectorArea,
				producer.solarCollector.collectorArea);
		}

		return new InvestmentItem(
			asset, type, producer,
			investment,
			investment,
			costs.duration,
			costs.repair,
			costs.maintenance,
			costs.operation
		);
	}

	@Nullable
	private static InvestmentItem itemOf(
		@Nullable AbstractProduct product, @Nullable ProductCosts costs
	) {
		return itemOf(product, costs, ProductType.OTHER_EQUIPMENT);
	}

	@Nullable
	private static InvestmentItem itemOf(
		@Nullable AbstractProduct product,
		@Nullable ProductCosts costs,
		ProductType defaultType
	) {
		if (product == null && ProductCosts.isEmpty(costs))
			return null;
		var asset = nameOf(product, defaultType);
		var type = typeOf(product, defaultType);
		if (costs == null)
			return new InvestmentItem(asset, type, null, 0, 0, 0, 0, 0, 0);
		return new InvestmentItem(
			asset, type, null,
			costs.investment,
			costs.investment,
			costs.duration,
			costs.repair,
			costs.maintenance,
			costs.operation
		);
	}

	private static ProductType typeOf(Producer producer) {
		if (producer == null)
			return ProductType.OTHER_EQUIPMENT;
		if (producer.boiler != null) {
			if (producer.boiler.type != null)
				return producer.boiler.type;
			if (producer.boiler.group != null && producer.boiler.group.type != null)
				return producer.boiler.group.type;
		}
		if (producer.productGroup != null && producer.productGroup.type != null)
			return producer.productGroup.type;
		return ProductType.OTHER_EQUIPMENT;
	}

	private static ProductType typeOf(
		AbstractProduct product, ProductType defaultType
	) {
		if (product == null)
			return defaultType;
		if (product.type != null)
			return product.type;
		if (product.group != null && product.group.type != null)
			return product.group.type;
		else
			return defaultType;
	}

	private static String nameOf(
		AbstractProduct product, ProductType defaultType
	) {
		if (product == null)
			return Labels.get(defaultType);
		if (Strings.isNotBlank(product.name))
			return product.name;
		if (product.group != null && Strings.isNotBlank(product.group.name))
			return product.group.name;
		var type = typeOf(product, defaultType);
		return Labels.get(type);
	}

}
