package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.CostResult;
import sophena.calc.CostResultItem;
import sophena.model.ProductType;
import sophena.rcp.Labels;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Enums;
import sophena.utils.Num;
import sophena.utils.Strings;

class CostDetailsTable {

	private CostResult result;

	private CostDetailsTable(CostResult result) {
		this.result = result;
	}

	static void create(CostResult result, Composite comp) {
		new CostDetailsTable(result).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp,
				"Produktbereich",
				"Produkt",
				"Investitionskosten",
				"Kapitalgebundene Kosten",
				"Bedarfsgebundene Kosten",
				"Betriebsgebundene Kosten");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 2, 3, 4, 5);
		double w = 1d / 6d - 0.001;
		Tables.bindColumnWidths(table, w, w, w, w, w, w);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		for (CostResultItem r : result.items) {
			Item item = new Item();
			item.type = r.productType;
			item.category = Labels.getPlural(r.productType);
			item.product = r.label;
			item.investment = r.costs.investment;
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
			int c = Enums.compare(a.type, b.type);
			if (c != 0)
				return c;
			return Strings.compare(a.product, b.product);
		});
		String last = "";
		for (Item item : items) {
			if (Strings.nullOrEqual(item.category, last)) {
				item.displayCategory = false;
			} else {
				item.displayCategory = true;
				last = item.category;
			}
		}
	}

	private String s(double value, String unit) {
		long v = Math.round(value);
		return Num.intStr(v) + " " + unit;
	}

	private static class Item {
		ProductType type;
		String category;
		String product;
		double investment;
		String capitalCosts;
		String consumptionCosts;
		String operationCosts;
		boolean displayCategory = true;
	}

	private class Label extends LabelProvider implements ITableLabelProvider,
			ITableColorProvider, ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public Color getBackground(Object obj, int col) {
			return null;
		}

		@Override
		public Color getForeground(Object obj, int col) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			if (col == 2 && item.investment == 0.0)
				return Colors.getSystemColor(SWT.COLOR_RED);
			return null;
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			if (col == 0 && item.displayCategory)
				return UI.boldFont();
			if (col == 2 && item.investment == 0.0)
				return UI.boldFont();
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
				return s(item.investment, "EUR");
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
