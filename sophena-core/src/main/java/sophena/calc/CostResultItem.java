package sophena.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.model.AbstractProduct;
import sophena.model.Consumer;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductType;
import sophena.model.Project;

public class CostResultItem {

	public String label;
	public ProductType productType;
	public ProductCosts costs;

	public double netCapitalCosts;
	public double grossCapitalCosts;

	public double netConsumtionCosts;
	public double grossConsumptionCosts;

	public double netOperationCosts;
	public double grossOperationCosts;

	static CostResultItem create(ProductEntry entry) {
		CostResultItem item = new CostResultItem();
		if (entry == null)
			return item;
		if (entry.product != null) {
			item.productType = entry.product.type;
			item.label = entry.product.name;
		}
		return copy(entry.costs, item);
	}

	static CostResultItem create(Producer producer) {
		CostResultItem item = new CostResultItem();
		if (producer == null)
			return item;
		item.label = producer.name;
		if (producer.boiler != null) {
			item.productType = producer.boiler.type;
			item.label = producer.boiler.name;
		}
		return copy(producer.costs, item);
	}

	static CostResultItem forBuffer(Project project) {
		if (project == null || project.heatNet == null)
			return new CostResultItem();
		CostResultItem item = init(project.heatNet.bufferTank,
				ProductType.BUFFER_TANK, "Pufferspeicher");
		return copy(project.heatNet.bufferTankCosts, item);
	}

	static List<CostResultItem> forPipes(Project project) {
		if (project == null || project.heatNet == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (HeatNetPipe pipe : project.heatNet.pipes) {
			if (!shouldAdd(pipe.pipe, pipe.costs))
				continue;
			CostResultItem item = init(pipe.pipe, ProductType.PIPE, "Wärmeleitung");
			copy(pipe.costs, item);
			items.add(item);
		}
		return items;
	}

	static List<CostResultItem> forTransferStations(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (Consumer c : project.consumers) {
			if (c.disabled)
				continue;
			if (!shouldAdd(c.transferStation, c.transferStationCosts))
				continue;
			CostResultItem item = init(c.transferStation,
					ProductType.TRANSFER_STATION,
					c.name + " - Hausübergabestation");
			items.add(copy(c.transferStationCosts, item));
		}
		return items;
	}

	static List<CostResultItem> forHeatRecoveries(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (Producer p : project.producers) {
			if (p.disabled)
				continue;
			if (!shouldAdd(p.heatRecovery, p.heatRecoveryCosts))
				continue;
			CostResultItem item = init(p.heatRecovery,
					ProductType.HEAT_RECOVERY,
					p.name + "- Wärmerückgewinnung");
			items.add(copy(p.heatRecoveryCosts, item));
		}
		return items;
	}

	static List<CostResultItem> forFlueGasCleanings(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (FlueGasCleaningEntry e : project.flueGasCleaningEntries) {
			if (!shouldAdd(e.product, e.costs))
				continue;
			CostResultItem item = init(e.product, ProductType.FLUE_GAS_CLEANING,
					"Rauchgasreinigung");
			items.add(copy(e.costs, item));
		}
		return items;
	}

	/**
	 * When a cost item is linked to a product it should be always displayed in
	 * the results even if no costs are provided for that product (because is is
	 * typically an error that should be shown to the user).
	 */
	private static boolean shouldAdd(AbstractProduct product,
			ProductCosts costs) {
		if (product != null)
			return true;
		return !ProductCosts.isEmpty(costs);
	}

	private static CostResultItem init(AbstractProduct product,
			ProductType defaultType, String defaultLabel) {
		CostResultItem item = new CostResultItem();
		if (product == null) {
			item.label = defaultLabel;
			item.productType = defaultType;
		} else {
			item.label = product.name;
			item.productType = product.type != null
					? product.type
					: defaultType;
		}
		return item;
	}

	private static CostResultItem copy(ProductCosts costs,
			CostResultItem item) {
		if (costs == null) {
			item.costs = new ProductCosts();
		} else {
			item.costs = costs.clone();
		}
		return item;
	}
}
