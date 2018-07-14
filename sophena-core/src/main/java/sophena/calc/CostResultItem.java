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

	static CostResultItem createForBuffer(HeatNet net) {
		if (net == null)
			return new CostResultItem();
		CostResultItem item = init(net.bufferTank,
				ProductType.BUFFER_TANK,
				"Pufferspeicher");
		return copy(net.bufferTankCosts, item);
	}

	static CostResultItem create(HeatNetPipe pipe) {
		if (pipe == null)
			return new CostResultItem();
		CostResultItem item = init(pipe.pipe,
				ProductType.PIPE,
				"Wärmeleitung");
		return copy(pipe.costs, item);
	}

	static List<CostResultItem> forTransferStations(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (Consumer consumer : project.consumers) {
			if (consumer.disabled)
				continue;
			if (ProductCosts.isEmpty(consumer.transferStationCosts))
				continue;
			CostResultItem item = init(consumer.transferStation,
					ProductType.TRANSFER_STATION,
					consumer.name + " - Hausübergabestation");
			items.add(copy(consumer.transferStationCosts, item));
		}
		return items;
	}

	static List<CostResultItem> forHeatRecoveries(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (Producer producer : project.producers) {
			if (producer.disabled)
				continue;
			if (ProductCosts.isEmpty(producer.heatRecoveryCosts))
				continue;
			CostResultItem item = init(producer.heatRecovery,
					ProductType.HEAT_RECOVERY,
					producer.name + "- Wärmerückgewinnung");
			items.add(copy(producer.heatRecoveryCosts, item));
		}
		return items;
	}

	static List<CostResultItem> forFlueGasCleanings(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (FlueGasCleaningEntry e : project.flueGasCleaningEntries) {
			if (ProductCosts.isEmpty(e.costs))
				continue;
			CostResultItem item = init(e.product,
					ProductType.FLUE_GAS_CLEANING,
					"Rauchgasreinigung");
			items.add(copy(e.costs, item));
		}
		return items;
	}

	private static CostResultItem init(AbstractProduct product,
			ProductType defaultType, String defaultLabel) {
		CostResultItem item = new CostResultItem();
		if (product == null) {
			item.label = defaultLabel;
			item.productType = defaultType;
		} else {
			item.label = product.name;
			item.productType = product.type != null ? product.type : defaultType;
		}
		return item;
	}

	private static CostResultItem copy(ProductCosts costs, CostResultItem item) {
		if (costs == null) {
			item.costs = new ProductCosts();
		} else {
			item.costs = costs.clone();
		}
		return item;
	}
}
