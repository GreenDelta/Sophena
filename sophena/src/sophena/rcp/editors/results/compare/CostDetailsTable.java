package sophena.rcp.editors.results.compare;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.CostResultItem;
import sophena.model.ProductType;
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
				"Aufschlüsselung der Investitionen");
		Table table = new Table(result);
		createItems(table);
		table.render(comp);
	}

	private void createItems(Table table) {
		Map<ProductType, double[]> details = calculate();
		double[] totals = new double[result.projects.length];
		for (ProductType type : ProductType.values()) {
			double[] values = details.get(type);
			if (values == null)
				continue;
			for (int i = 0; i < totals.length; i++) {
				totals[i] += values[i];
			}
			table.row(Labels.get(type),
					i -> Num.intStr(values[i]) + " EUR");
		}
		table.emptyRow();
		table.boldRow("Investitionssumme",
				i -> Num.intStr(totals[i]) + " EUR");
	}

	private Map<ProductType, double[]> calculate() {
		Map<ProductType, double[]> r = new HashMap<>();
		int length = result.projects.length;
		for (int i = 0; i < length; i++) {
			CostResult cr = result.results[i].costResult;
			for (CostResultItem item : cr.items) {
				if (item.costs == null || item.costs.investment == 0d)
					continue;
				double[] values = r.computeIfAbsent(item.productType,
						t -> new double[length]);
				values[i] += item.costs.investment;
			}
		}
		return r;
	}

}
