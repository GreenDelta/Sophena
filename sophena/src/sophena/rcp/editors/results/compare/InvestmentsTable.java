package sophena.rcp.editors.results.compare;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProductAreaResult;
import sophena.model.ProductArea;
import sophena.rcp.Labels;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class InvestmentsTable {

	private final Comparison result;

	private InvestmentsTable(Comparison result) {
		this.result = result;
	}

	static InvestmentsTable of(Comparison result) {
		return new InvestmentsTable(result);
	}

	void render(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk,
				"Investitionskosten");
		Table table = new Table(result);
		createItems(table);
		table.render(comp);
	}

	private void createItems(Table table) {
		ProductAreaResult[] details = results();
		for (ProductArea area : ProductArea.values()) {
			if (allZero(details, area))
				continue;
			table.row(Labels.get(area), i -> {
				double val = details[i].investmentCosts(area);
				return Num.intStr(val) + " EUR";
			});
		}
		table.emptyRow();
		table.boldRow("Investitionssumme",
				i -> Num.intStr(details[i].totalInvestmentCosts) + " EUR");
	}

	private ProductAreaResult[] results() {
		int length = result.projects.length;
		ProductAreaResult[] r = new ProductAreaResult[length];
		for (int i = 0; i < length; i++) {
			CostResult cr = result.results[i].costResultFunding;
			r[i] = ProductAreaResult.calculate(cr);
		}
		return r;
	}

	private boolean allZero(ProductAreaResult[] r, ProductArea area) {
		for (ProductAreaResult par : r) {
			if (par.investmentCosts(area) != 0)
				return false;
		}
		return true;
	}

}
