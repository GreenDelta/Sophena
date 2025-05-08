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

	private EfficiencyResult r;

	private EfficiencyTable(EfficiencyResult result) {
		this.r = result;
	}

	static void create(EfficiencyResult result, Composite comp) {
		new EfficiencyTable(result).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "", "Absolut",
				"Prozentual");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 1, 2);
		//Tables.autoSizeColumns(table);
		table.getTable().getColumn(0).setWidth(200);
		table.getTable().getColumn(1).setWidth(200);
		table.getTable().getColumn(2).setWidth(200);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		if (r.producedElectrictiy > 0) {
			items.add(new Item("Erzeugter Strom", r.producedElectrictiy));
		}
		items.add(new Item("Erzeugte Wärme", r.producedHeat));
		items.add(new Item("Pufferspeicherverluste", r.bufferLoss,
				r.producedHeat));
		items.add(new Item("Verteilungsverluste", r.distributionLoss,
				r.producedHeat));
		items.add(new Item("Genutzte Wärme", r.usedHeat));
		items.add(new Item());
		Item total = new Item("Gesamtverluste", r.totalLoss, r.producedHeat);
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

		Item(String label, double absolute, double total) {
			this(label, absolute);
			double rel = total == 0 ? 0 : absolute / total;
			this.relative = Num.intStr(rel * 100) + "%";
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
