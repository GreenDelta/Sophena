package sophena.rcp.editors.results.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.rcp.utils.Tables;
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
		String[] headers = new String[1 + result.projects.length];
		headers[0] = "";
		int[] aligns = new int[headers.length - 1];
		double[] widths = new double[headers.length];
		double width = 1.0 / headers.length;
		widths[0] = width;
		for (int i = 0; i < result.projects.length; i++) {
			String name = result.projects[i].name;
			name = name == null ? "?" : name;
			headers[i + 1] = name;
			widths[i + 1] = width;
			aligns[i] = i + 1;
		}
		TableViewer table = Tables.createViewer(comp, headers);
		Tables.bindColumnWidths(table, widths);
		table.setLabelProvider(new Label());
		Tables.rightAlignColumns(table, aligns);
		table.setInput(createItems());
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		items.add(item("Investitionskosten",
				idx -> Num.intStr(costs(idx).investments) + " EUR"));
		if (withFunding) {
			items.add(item("Investitionsförderung", idx -> {
				// TODO: total funding ...
				double funding = result.projects[idx].costSettings.funding;
				return Num.intStr(funding) + " EUR";
			}));
		}
		items.add(item("Kapitalgebundene Kosten",
				idx -> Num.intStr(costs(idx).capitalCosts) + " EUR/a"));
		items.add(item("Bedarfsgebundene Kosten",
				idx -> Num.intStr(costs(idx).consumptionCosts) + " EUR/a"));
		items.add(item("Betriebsgebundene Kosten",
				idx -> Num.intStr(costs(idx).operationCosts) + " EUR/a"));
		items.add(item("Sonstige Kosten",
				idx -> Num.intStr(costs(idx).otherCosts) + " EUR/a"));
		items.add(item("Erlöse",
				idx -> Num.intStr(costs(idx).revenues) + " EUR/a"));
		items.add(item("Kosten - Erlöse",
				idx -> Num.intStr(costs(idx).annualCosts) + " EUR/a"));
		items.add(item("Wärmegestehungskosten",
				idx -> Num.intStr(costs(idx).heatGenerationCosts * 1000)
						+ " EUR/MWh"));
		return items;
	}

	private Item item(String aspect, IntFunction<String> fn) {
		Item item = new Item();
		item.aspect = aspect;
		item.results = new String[result.projects.length];
		for (int i = 0; i < result.projects.length; i++) {
			item.results[i] = fn.apply(i);
		}
		return item;
	}

	private CostResult.FieldSet costs(int idx) {
		if (idx >= result.results.length)
			return new CostResult.FieldSet();
		return withFunding
				? result.results[idx].costResultFunding.netTotal
				: result.results[idx].costResult.netTotal;
	}

	private class Item {
		String aspect;
		String[] results;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			if (col == 0)
				return item.aspect;
			int idx = col - 1;
			if (item.results == null || idx >= item.results.length)
				return null;
			return item.results[idx];
		}
	}
}
