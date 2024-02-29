package sophena.rcp.editors.results.single;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import sophena.calc.CO2Result;
import sophena.model.Producer;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class EmissionTable {

	private final CO2Result result;

	private EmissionTable(CO2Result result) {
		this.result = result;
	}

	public static void create(CO2Result result, Composite comp) {
		new EmissionTable(result).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "", "Emissionen");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 1);		
		//Tables.autoSizeColumns(table);		
		table.getTable().getColumn(0).setWidth(200);
		table.getTable().getColumn(1).setWidth(200);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		Map<Producer, Double> m = result.producerEmissions;
		for (Producer p : m.keySet()) {
			Double val = m.get(p);
			items.add(new Item(p.name, val == null ? 0 : val));
		}
		addElectricityItems(items);
		Item totalItem = new Item("Wärmenetz", result.total);
		totalItem.total = true;
		items.add(new Item());
		items.add(totalItem);
		items.add(new Item());
		items.add(new Item("Erdgas dezentral", result.variantNaturalGas));
		items.add(new Item("Heizöl dezentral", result.variantOil));
		return items;
	}

	private void addElectricityItems(List<Item> items) {
		items.add(new Item("Eigenstromverbrauch", result.electricityEmissions));
		double credits = result.electricityCredits;
		if (credits > 0) {
			items.add(new Item("Gutschrift Stromerzeugung", -credits));
		}
	}

	private static class Item {
		String label;
		String emissions;
		boolean total = false;

		Item() {
		}

		Item(String label, double value) {
			this.label = label;
			co2(value);
		}

		void co2(double value) {
			emissions = Num.intStr(value) + " kg CO2 eq.";
		}
	}

	private static class Label extends LabelProvider
			implements ITableLabelProvider, IFontProvider {

		@Override
		public Font getFont(Object obj) {
			if (!(obj instanceof Item item))
				return null;
			if (item.total)
				return UI.boldFont();
			return null;
		}

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item item))
				return null;
			return switch (col) {
				case 0 -> item.label;
				case 1 -> item.emissions;
				default -> null;
			};
		}
	}
}
