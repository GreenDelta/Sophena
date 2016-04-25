package sophena.calc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductType;

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
		CostResultItem item = new CostResultItem();
		if (net == null)
			return item;
		if (net.bufferTank == null) {
			item.label = "Pufferspeicher";
			item.productType = ProductType.BUFFER_TANK;
		} else {
			item.label = net.bufferTank.name;
			item.productType = net.bufferTank.type;
		}
		return copy(net.bufferTankCosts, item);
	}

	static CostResultItem create(HeatNetPipe pipe) {
		CostResultItem item = new CostResultItem();
		if (pipe == null)
			return item;
		if (pipe.pipe == null) {
			item.label = "Wärmeleitung";
			item.productType = ProductType.HEATING_NET_TECHNOLOGY;
		} else {
			item.label = pipe.pipe.name;
			item.productType = pipe.pipe.type;
		}
		return copy(pipe.costs, item);
	}

	static List<CostResultItem> forTransferStations(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (Consumer consumer : project.consumers) {
			if (consumer.transferStation == null)
				continue;
			CostResultItem item = new CostResultItem();
			item.label = consumer.transferStation.name;
			item.productType = ProductType.TRANSFER_STATION;
			items.add(copy(consumer.transferStationCosts, item));
		}
		return items;
	}
	
	static List<CostResultItem> forHeatRecoveries(Project project) {
		if (project == null)
			return Collections.emptyList();
		List<CostResultItem> items = new ArrayList<>();
		for (Producer producer : project.producers) {
			if (ProductCosts.empty(producer.heatRecoveryCosts))
				continue;
			// TODO: create item
		}
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
