package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import sophena.math.energetic.CO2Emissions;
import sophena.model.Producer;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class EmissionTable {

	private CO2Emissions result;

	private EmissionTable(CO2Emissions result) {
		this.result = result;
	}

	public static void create(CO2Emissions result, Composite comp) {
		new EmissionTable(result).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "", "Emissionen");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 1);
		Tables.autoSizeColumns(table);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		Map<Producer, Double> m = result.producerEmissions;
		for (Producer p : m.keySet()) {
			Double val = m.get(p);
			items.add(new Item(p.name, val == null ? 0 : val));
		}
		addCreditsItem(items);
		Item totalItem = new Item("Wärmenetz", result.total);
		totalItem.total = true;
		items.add(new Item());
		items.add(totalItem);
		items.add(new Item());
		items.add(new Item("Variante Erdgas", result.variantNaturalGas));
		items.add(new Item("Variante Heizöl", result.variantOil));
		return items;
	}

	private void addCreditsItem(List<Item> items) {
		double credits = result.electricityCredits;
		if (credits > 0) {
			items.add(new Item("Gutschrift Stromerzeugung", -credits));
		}
	}

	private class Item {
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

	private class Label extends LabelProvider implements ITableLabelProvider,
			IFontProvider {

		@Override
		public Font getFont(Object obj) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
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
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			switch (col) {
			case 0:
				return item.label;
			case 1:
				return item.emissions;
			default:
				return null;
			}
		}
	}
}
