package sophena.calc;

import java.util.EnumMap;

import sophena.model.ProductArea;

/**
 * The ProductAreaResult is an aggregation of the single result items in the
 * cost result of a project by product area.
 */
public class ProductAreaResult {

	private final EnumMap<ProductArea, Entry> data = new EnumMap<>(
			ProductArea.class);

	private ProductAreaResult() {
	}

	public static ProductAreaResult calculate(CostResult r) {
		ProductAreaResult par = new ProductAreaResult();
		if (r == null)
			return par;
		for (CostResultItem item : r.items) {
			par.add(item);
		}
		return par;
	}

	private void add(CostResultItem item) {
		if (item == null
				|| item.productType == null
				|| item.productType.productArea == null)
			return;
		ProductArea area = item.productType.productArea;
		Entry entry = data.get(area);
		if (entry == null) {
			entry = new Entry();
			data.put(area, entry);
		}
		if (item.costs != null) {
			entry.investmentCosts += item.costs.investment;
		}
		entry.capitalCosts += item.netCapitalCosts;
		entry.demandRelatedCosts += item.netConsumtionCosts;
		entry.operationRelatedCosts += item.netOperationCosts;
	}

	public double investmentCosts(ProductArea area) {
		if (area == null)
			return 0.0;
		Entry e = data.get(area);
		if (e == null)
			return 0.0;
		return e.investmentCosts;
	}

	public double capitalCosts(ProductArea area) {
		if (area == null)
			return 0.0;
		Entry e = data.get(area);
		if (e == null)
			return 0.0;
		return e.capitalCosts;
	}

	public double demandRelatedCosts(ProductArea area) {
		if (area == null)
			return 0.0;
		Entry e = data.get(area);
		if (e == null)
			return 0.0;
		return e.demandRelatedCosts;
	}

	public double operationRelatedCosts(ProductArea area) {
		if (area == null)
			return 0.0;
		Entry e = data.get(area);
		if (e == null)
			return 0.0;
		return e.operationRelatedCosts;
	}

	private class Entry {
		double investmentCosts;
		double capitalCosts;
		double demandRelatedCosts;
		double operationRelatedCosts;
	}

}
