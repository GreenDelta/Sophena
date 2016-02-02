package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import sophena.math.energetic.EfficiencyResult;
import sophena.rcp.utils.Tables;
import sophena.utils.Num;

class EfficiencyTable {

	private EfficiencyResult result;

	private EfficiencyTable(EfficiencyResult result) {
		this.result = result;
	}

	static void create(EfficiencyResult result, Composite comp) {
		new EfficiencyTable(result).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "", "Absolut", "Prozentual");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 1, 2);
		Tables.autoSizeColumns(table);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		items.add(new Item("Brennstoffenergie", result.fuelEnergy));
		items.add(new Item("Konversionsverluste", result.conversionLoss,
				result.conversionLoss / result.fuelEnergy));
		items.add(new Item("Erzeugte Wärme", result.producedHeat));
		if (result.producedElectrictiy > 0)
			items.add(new Item("Erzeugter Strom", result.producedElectrictiy));
		items.add(new Item("Verteilungsverluste", result.distributionLoss,
				result.distributionLoss / result.producedHeat));
		items.add(new Item("Genutzte Wärme", result.usedHeat));
		items.add(new Item());
		Item total = new Item("Gesamtverluste", result.totalLoss,
				result.totalLoss / result.fuelEnergy);
		total.total = true;
		items.add(total);
		return items;
	}

	private class Item {
		String label;
		String absolute;
		String relative;
		boolean total = false;

		Item() {
		}

		Item(String label, double absolute) {
			this.label = label;
			this.absolute = Num.intStr(absolute) + " kWh";
		}

		Item(String label, double absolute, double relative) {
			this(label, absolute);
			this.relative = Num.intStr(relative * 100) + "%";
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
				return JFaceResources.getFontRegistry().getBold(
						JFaceResources.DEFAULT_FONT);
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
				return item.absolute;
			case 2:
				return item.relative;
			default:
				return null;
			}
		}
	}

}
