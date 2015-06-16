package sophena.rcp.editors.results;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.ProjectResult;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Stats;
import sophena.rcp.Numbers;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class BoilerTableSection {

	private ProjectResult result;

	BoilerTableSection(ProjectResult result) {
		this.result = result;
	}

	public void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Wärmelieferanten");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, "Wärmelieferant", "Rang",
				"Gelieferte Wärme", "Anteil", "Volllaststunden");
		table.setLabelProvider(new Label());
		table.setContentProvider(ArrayContentProvider.getInstance());
		Tables.bindColumnWidths(table, 0.2, 0.2, 0.2, 0.2, 0.2);
		table.setInput(getItems());
	}

	private List<Item> getItems() {
		List<Item> items = new ArrayList<>();
		Producer[] producers = result.getProducers();
		if (producers == null)
			return Collections.emptyList();
		for (int i = 0; i < producers.length; i++) {
			Producer p = producers[i];
			Item item = new Item();
			item.name = p.getName();
			if (p.getFunction() == ProducerFunction.BASE_LOAD)
				item.rank = p.getRank() + " - Grundlast";
			else
				item.rank = p.getRank() + " - Spitzenlast";
			item.pos = i;
			item.heat = Stats.sum(result.getProducerResults()[i]);
			items.add(item);
		}
		Item bufferItem = new Item();
		bufferItem.pos = producers.length;
		bufferItem.name = "Pufferspeicher";
		items.add(bufferItem);
		bufferItem.heat = Stats.sum(result.getSuppliedBufferHeat());
		calculateShares(items);
		return items;
	}

	private void calculateShares(List<Item> items) {
		double sum = 0;
		for (Item item : items)
			sum += item.heat;
		if (sum == 0)
			return;
		for (Item item : items)
			item.share = Math.round(100 * item.heat / sum);
	}

	private class Item {
		int pos;
		String name;
		String rank;
		double heat;
		double share;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		private ColorImage img = new ColorImage(UI.shell().getDisplay());

		@Override
		public Image getColumnImage(Object element, int col) {
			if (!(element instanceof Item) || col != 0)
				return null;
			Item item = (Item) element;
			return img.get(item.pos);
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Item))
				return null;
			Item item = (Item) element;
			switch (col) {
			case 0:
				return item.name;
			case 1:
				return item.rank;
			case 2:
				return Numbers.toString(item.heat) + " kWh";
			case 3:
				return Numbers.toString(item.share) + " %";
			default:
				return null;
			}
		}

		@Override
		public void dispose() {
			img.dispose();
			super.dispose();
		}
	}

}
