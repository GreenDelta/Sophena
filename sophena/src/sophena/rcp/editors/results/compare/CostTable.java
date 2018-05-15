package sophena.rcp.editors.results.compare;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class CostTable {

	private final Comparison result;
	private boolean withFunding;

	private CostTable(Comparison result) {
		this.result = result;
	}

	static CostTable of(Comparison result) {
		return new CostTable(result);
	}

	CostTable withFunding() {
		this.withFunding = true;
		return this;
	}

	void render(Composite body, FormToolkit tk) {
		String title = withFunding
				? "Übersicht (mit Förderung)"
				: "Übersicht (ohne Förderung)";
		Composite comp = UI.formSection(body, tk, title);
		Table table = new Table(result);
		createItems(table);
		table.render(comp);
	}

	private void createItems(Table table) {
		table.item("Investitionskosten",
				idx -> Num.intStr(costs(idx).investments) + " EUR");
		if (withFunding) {
			table.item("Investitionsförderung", idx -> {
				// TODO: total funding ...
				double funding = result.projects[idx].costSettings.funding;
				return Num.intStr(funding) + " EUR";
			});
		}
		table.item("Kapitalgebundene Kosten",
				idx -> Num.intStr(costs(idx).capitalCosts) + " EUR/a");
		table.item("Bedarfsgebundene Kosten",
				idx -> Num.intStr(costs(idx).consumptionCosts) + " EUR/a");
		table.item("Betriebsgebundene Kosten",
				idx -> Num.intStr(costs(idx).operationCosts) + " EUR/a");
		table.item("Sonstige Kosten",
				idx -> Num.intStr(costs(idx).otherCosts) + " EUR/a");
		table.item("Erlöse",
				idx -> Num.intStr(costs(idx).revenues) + " EUR/a");
		table.item("Kosten - Erlöse",
				idx -> Num.intStr(costs(idx).annualCosts) + " EUR/a");
		table.item("Wärmegestehungskosten",
				idx -> Num.intStr(costs(idx).heatGenerationCosts * 1000)
						+ " EUR/MWh");
	}

	private CostResult.FieldSet costs(int idx) {
		if (idx >= result.results.length)
			return new CostResult.FieldSet();
		return withFunding
				? result.results[idx].costResultFunding.netTotal
				: result.results[idx].costResult.netTotal;
	}

}
