package sophena.rcp.editors.results.compare;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.CostResultItem;
import sophena.model.ProductArea;
import sophena.rcp.Labels;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class CostDetailsTable {

	private final Comparison result;

	private CostDetailsTable(Comparison result) {
		this.result = result;
	}

	static CostDetailsTable of(Comparison result) {
		return new CostDetailsTable(result);
	}

	void render(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk,
				"Investitionskosten");
		Table table = new Table(result);
		createItems(table);
		table.render(comp);
	}

	private void createItems(Table table) {
		Map<ProductArea, double[]> details = calculate();
		double[] totals = new double[result.projects.length];
		for (ProductArea area : ProductArea.values()) {
			double[] values = details.get(area);
			if (values == null)
				continue;
			for (int i = 0; i < totals.length; i++) {
				totals[i] += values[i];
			}
			table.row(Labels.get(area),
					i -> Num.intStr(values[i]) + " EUR");
		}
		table.emptyRow();
		table.boldRow("Investitionssumme",
				i -> Num.intStr(totals[i]) + " EUR");
	}

	private Map<ProductArea, double[]> calculate() {
		int length = result.projects.length;
		Map<ProductArea, double[]> r = new HashMap<>();
		for (ProductArea pa : ProductArea.values()) {
			r.put(pa, new double[length]);
		}
		for (int i = 0; i < length; i++) {
			CostResult cr = result.results[i].costResult;
			for (CostResultItem item : cr.items) {
				if (item.productType == null
						|| item.costs == null
						|| item.costs.investment == 0d)
					continue;
				double[] values = r.get(item.productType.productArea);
				values[i] += item.costs.investment;
			}
		}
		return r;
	}

}
