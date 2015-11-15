package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import sophena.calc.CostResult;
import sophena.calc.CostResultItem;
import sophena.rcp.Labels;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;

class CostDetailsTable {

	private CostResult result;

	private CostDetailsTable(CostResult result) {
		this.result = result;
	}

	static void create(CostResult result, Composite comp) {
		new CostDetailsTable(result).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Kategorie", "Komponente",
				"Investitionskosten", "Kapitalgebundene Kosten",
				"Bedarfsgebundene Kosten", "Betriebsgebundene Kosten");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Table t = table.getTable();
		t.getColumn(2).setAlignment(SWT.RIGHT);
		t.getColumn(3).setAlignment(SWT.RIGHT);
		t.getColumn(4).setAlignment(SWT.RIGHT);
		t.getColumn(5).setAlignment(SWT.RIGHT);
		double w = 1d / 6d - 0.001;
		Tables.bindColumnWidths(table, w, w, w, w, w, w);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		for (CostResultItem r : result.items) {
			Item item = new Item();
			if (r.group != null) {
				item.category = Labels.get(r.group.type);
			}
			if (item.category == null)
				item.category = "Andere";
			item.product = r.label;
			item.investment = s(r.costs.investment, "EUR");
			item.capitalCosts = s(r.netCapitalCosts, "EUR/a");
			item.consumptionCosts = s(r.netConsumtionCosts, "EUR/a");
			item.operationCosts = s(r.netOperationCosts, "EUR/a");
			items.add(item);
		}
		sortAndRefine(items);
		return items;
	}

	private void sortAndRefine(List<Item> items) {
		items.sort((a, b) -> {
			int c = Strings.compare(a.category, b.category);
			if (c != 0)
				return c;
			return Strings.compare(a.product, b.product);
		});
		for (int i = 1; i < items.size(); i++) {
			Item before = items.get(i - 1);
			Item after = items.get(i);
			if (Strings.nullOrEqual(before.category, after.category))
				after.displayCategory = false;
		}
	}

	private String s(double value, String unit) {
		long v = Math.round(value);
		return Long.toString(v) + " " + unit;
	}

	private class Item {
		String category;
		String product;
		String investment;
		String capitalCosts;
		String consumptionCosts;
		String operationCosts;
		boolean displayCategory = true;
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
			switch (col) {
			case 0:
				return item.displayCategory ? item.category : null;
			case 1:
				return item.product;
			case 2:
				return item.investment;
			case 3:
				return item.capitalCosts;
			case 4:
				return item.consumptionCosts;
			case 5:
				return item.operationCosts;
			default:
				return null;
			}
		}

	}
}