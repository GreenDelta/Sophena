package sophena.rcp.editors.results.compare;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.CostResult.FieldSet;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class CostTable {

	private final Comparison result;

	private CostTable(Comparison result) {
		this.result = result;
	}

	static CostTable of(Comparison result) {
		return new CostTable(result);
	}

	void render(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "Wirtschaftlichkeit");
		Table table = new Table(result);
		createItems(table);
		table.render(comp);
	}

	private void createItems(Table t) {
		// investment costs
		t.row("Investitionskosten",
				idx -> Num.intStr(costs(idx).investments) + " EUR");
		t.row("Investitionsförderung",
				idx -> Num.intStr(costs(idx).funding) + " EUR");
		t.row("Anschlusskostenbeiträge", idx -> {
			double c = result.projects[idx].costSettings.connectionFees;
			return Num.intStr(c) + " EUR";
		});
		t.boldRow("Finanzierungsbedarf", idx -> {
			FieldSet costs = costs(idx);
			double cf = result.projects[idx].costSettings.connectionFees;
			double s = costs.investments - costs.funding - cf;
			return Num.intStr(s) + " EUR";
		});
		t.emptyRow();

		// annual costs
		t.row("Kapitalgebundene Kosten",
				idx -> Num.intStr(costs(idx).capitalCosts) + " EUR/a");
		t.row("Bedarfsgebundene Kosten",
				idx -> Num.intStr(costs(idx).consumptionCosts) + " EUR/a");
		t.row("Betriebsgebundene Kosten",
				idx -> Num.intStr(costs(idx).operationCosts) + " EUR/a");
		t.row("Sonstige Kosten",
				idx -> Num.intStr(costs(idx).otherAnnualCosts) + " EUR/a");
		t.boldRow("Gesamtkosten",
				idx -> Num.intStr(costs(idx).totalAnnualCosts) + " EUR/a");
		t.emptyRow();

		// revenues
		t.row("Wärmeerlöse",
				idx -> Num.intStr(costs(idx).revenuesHeat) + " EUR/a");
		t.row("Stromerlöse",
				idx -> Num.intStr(costs(idx).revenuesElectricity) + " EUR/a");
		t.boldRow("Gesamterlöse", idx -> {
			FieldSet costs = costs(idx);
			double revs = costs.revenuesElectricity + costs.revenuesHeat;
			return Num.intStr(revs) + " EUR/a";
		});
		t.emptyRow();

		t.boldRow("Jahresüberschuss",
				idx -> Num.intStr(costs(idx).annualSurplus) + " EUR/a");
		t.boldRow("Wärmegestehungskosten",
				idx -> Num.intStr(costs(idx).heatGenerationCosts) + " EUR/MWh");
	}

	private CostResult.FieldSet costs(int idx) {
		if (idx >= result.results.length)
			return new CostResult.FieldSet();
		return result.results[idx].costResultFunding.dynamicTotal;
	}

}
