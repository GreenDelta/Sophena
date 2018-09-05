package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import sophena.Labels;
import sophena.calc.CostResult;
import sophena.calc.ProductAreaResult;
import sophena.model.ProductArea;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class ProductAreaTable {

	private final ProductAreaResult result;

	private ProductAreaTable(ProductAreaResult result) {
		this.result = result;
	}

	static void create(CostResult r, Composite comp) {
		ProductAreaResult result = ProductAreaResult.calculate(r);
		new ProductAreaTable(result).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp,
				"Produktgebiet",
				"Investitionskosten",
				"Kapitalgebundene Kosten",
				"Bedarfsgebundene Kosten",
				"Betriebsgebundene Kosten");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 1, 2, 3, 4);
		double w = 1d / 5d - 0.001;
		Tables.bindColumnWidths(table, w, w, w, w, w);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		for (ProductArea area : ProductArea.values()) {
			if (allZero(area))
				continue;
			Item item = new Item();
			item.productArea = Labels.get(area);
			item.investmentCosts = Num.intStr(
					result.investmentCosts(area)) + " EUR";
			item.capitalCosts = Num.intStr(
					result.capitalCosts(area)) + "EUR/a";
			item.demandRelatedCosts = Num.intStr(
					result.demandRelatedCosts(area)) + "EUR/a";
			item.operationRelatedCosts = Num.intStr(
					result.operationRelatedCosts(area)) + "EUR/a";
			items.add(item);
		}
		items.add(new Item()); // empty row
		items.add(createTotal());
		return items;
	}

	private Item createTotal() {
		Item item = new Item();
		item.investmentCosts = Num.intStr(
				result.totalInvestmentCosts) + "EUR";
		item.capitalCosts = Num.intStr(
				result.totalCapitalCosts) + "EUR/a";
		item.demandRelatedCosts = Num.intStr(
				result.totalDemandRelatedCosts) + "EUR/a";
		item.operationRelatedCosts = Num.intStr(
				result.totalOperationRelatedCosts) + "EUR/a";
		return item;
	}

	private boolean allZero(ProductArea area) {
		return result.investmentCosts(area) == 0.0
				&& result.capitalCosts(area) == 0.0
				&& result.demandRelatedCosts(area) == 0.0
				&& result.operationRelatedCosts(area) == 0.0;
	}

	private static class Item {
		String productArea;
		String investmentCosts;
		String capitalCosts;
		String demandRelatedCosts;
		String operationRelatedCosts;
	}

	private class Label extends LabelProvider implements ITableLabelProvider,
			ITableFontProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			if (item.productArea == null
					&& item.investmentCosts != null)
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
				return item.productArea;
			case 1:
				return item.investmentCosts;
			case 2:
				return item.capitalCosts;
			case 3:
				return item.demandRelatedCosts;
			case 4:
				return item.operationRelatedCosts;
			default:
				return null;
			}
		}
	}
}
