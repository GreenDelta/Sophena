package sophena.calc;

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
			item.productType = producer.boiler.type;
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
		if (net.bufferTank == null) {
			item.label = "Pufferspeicher";
			item.productType = ProductType.BUFFER_TANK;
		} else {
			item.label = net.bufferTank.name;
			item.productType = net.bufferTank.type;
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
		if (pipe.pipe == null) {
			item.label = "Wärmeleitung";
			item.productType = ProductType.HEATING_NET_TECHNOLOGY;
		} else {
			item.label = pipe.pipe.name;
			item.productType = pipe.pipe.type;
		}
		if (pipe.costs != null)
			item.costs = pipe.costs.clone();
		else
			item.costs = new ProductCosts();
		return item;
	}

}
