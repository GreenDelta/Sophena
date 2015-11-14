package sophena.calc;

import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductGroup;

public class CostResultItem {

	public ProductGroup group;
	public String label;
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
			item.group = entry.product.group;
			item.label = entry.product.name;
		}
		if (entry.costs != null)
			item.costs = entry.costs.clone();
		else
			item.costs = new ProductCosts();
		return item;
	}

	static CostResultItem create(Producer producer) {
		CostResultItem item = new CostResultItem();
		if (producer == null)
			return item;
		item.label = producer.name;
		if (producer.boiler != null) {
			item.group = producer.boiler.group;
			item.label = producer.boiler.name;
		}
		if (producer.costs != null)
			item.costs = producer.costs.clone();
		else
			item.costs = new ProductCosts();
		return item;
	}

	static CostResultItem createForBuffer(HeatNet net) {
		CostResultItem item = new CostResultItem();
		if (net == null)
			return item;
		if (net.bufferTank == null)
			item.label = "Pufferspeicher";
		else {
			item.label = net.bufferTank.name;
			item.group = net.bufferTank.group;
		}
		if (net.bufferTankCosts != null)
			item.costs = net.bufferTankCosts.clone();
		else
			item.costs = new ProductCosts();
		return item;
	}

	static CostResultItem create(HeatNetPipe pipe) {
		CostResultItem item = new CostResultItem();
		if (pipe == null)
			return item;
		if (pipe.pipe == null)
			item.label = "Wärmeleitung";
		else {
			item.label = pipe.pipe.name;
			item.group = pipe.pipe.group;
		}
		if (pipe.costs != null)
			item.costs = pipe.costs.clone();
		else
			item.costs = new ProductCosts();
		return item;
	}

}
