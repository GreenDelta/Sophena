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
				? "Wirtschaftlichkeit - mit Förderung"
				: "Wirtschaftlichkeit - ohne Förderung";
		Composite comp = UI.formSection(body, tk, title);
		Table table = new Table(result);
		createItems(table);
		table.render(comp);
	}

	private void createItems(Table table) {
		table.row("Investitionskosten",
				idx -> Num.intStr(costs(idx).investments) + " EUR");
		if (withFunding) {
			table.row("Investitionsförderung",
					idx -> Num.intStr(costs(idx).funding) + " EUR");
		}
		table.row("Kapitalgebundene Kosten",
				idx -> Num.intStr(costs(idx).capitalCosts) + " EUR/a");
		table.row("Bedarfsgebundene Kosten",
				idx -> Num.intStr(costs(idx).consumptionCosts) + " EUR/a");
		table.row("Betriebsgebundene Kosten",
				idx -> Num.intStr(costs(idx).operationCosts) + " EUR/a");
		table.row("Sonstige Kosten",
				idx -> Num.intStr(costs(idx).otherCosts) + " EUR/a");
		table.row("Wärmeerlöse",
				idx -> Num.intStr(costs(idx).revenuesHeat) + " EUR/a");
		table.row("Stromerlöse",
				idx -> Num.intStr(costs(idx).revenuesElectricity) + " EUR/a");
		table.row("Jahresüberschuss",
				idx -> Num.intStr(costs(idx).annualSurplus) + " EUR/a");
		table.emptyRow();
		table.boldRow("Wärmegestehungskosten",
				idx -> Num.intStr(costs(idx).heatGenerationCosts) + " EUR/MWh");
	}

	private CostResult.FieldSet costs(int idx) {
		if (idx >= result.results.length)
			return new CostResult.FieldSet();
		return withFunding
				? result.results[idx].costResultFunding.netTotal
				: result.results[idx].costResult.netTotal;
	}

}
