package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.EnergyResult;
import sophena.model.Boiler;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Stats;
import sophena.rcp.Numbers;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class BoilerTableSection {

	private EnergyResult result;

	BoilerTableSection(EnergyResult result) {
		this.result = result;
	}

	public void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Wärmelieferanten");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, "Wärmelieferant",
				"Nennleistung", "Brennstoff", "Rang", "Gelieferte Wärme",
				"Anteil", "Volllaststunden");
		table.setLabelProvider(new Label());
		double w = 1d / 7d;
		Tables.bindColumnWidths(table, w, w, w, w, w, w, w);
		table.setInput(getItems());
	}

	private List<Item> getItems() {
		List<Item> items = new ArrayList<>();
		if (result.producers == null)
			return Collections.emptyList();
		initProducerItems(result.producers, items);
		addBufferItem(items, result.producers);
		addDiffItem(items);
		calculateShares(items);
		return items;
	}

	private void initProducerItems(Producer[] producers, List<Item> items) {
		for (int i = 0; i < producers.length; i++) {
			Producer p = producers[i];
			Item item = new Item();
			item.name = p.name;
			item.fuel = getFuel(p);
			double power = p.boiler != null ? p.boiler.maxPower : 0;
			item.power = Numbers.toString(power) + " kW";
			if (p.function == ProducerFunction.BASE_LOAD)
				item.rank = p.rank + " - Grundlast";
			else
				item.rank = p.rank + " - Spitzenlast";
			item.pos = i;
			item.heat = result.totalHeat(p);
			item.fullLoadHours = getFullLoadHours(p, item.heat);
			items.add(item);
		}
	}

	private String getFuel(Producer p) {
		Boiler b = p.boiler;
		if (b != null && b.fuel != null)
			return b.fuel.name;
		FuelSpec fs = p.fuelSpec;
		if (fs != null && fs.woodFuel != null)
			return fs.woodFuel.name;
		else
			return null;
	}

	private Integer getFullLoadHours(Producer p, double producedHeat) {
		if (p == null || p.boiler == null)
			return null;
		double maxPower = p.boiler.maxPower;
		return (int) Math.round(producedHeat / maxPower);
	}

	private void calculateShares(List<Item> items) {
		double load = result.totalLoad;
		if (load == 0)
			return;
		for (Item item : items) {
			double share = Math.round(100 * item.heat / load);
			item.share = share > 100 ? 100 : share;
		}
	}

	private void addBufferItem(List<Item> items, Producer[] producers) {
		Item bufferItem = new Item();
		bufferItem.pos = producers.length;
		bufferItem.name = "Pufferspeicher";
		items.add(bufferItem);
		bufferItem.heat = result.totalBufferedHeat;
	}

	private void addDiffItem(List<Item> items) {
		double diff = 0;
		for (int i = 0; i < Stats.HOURS; i++) {
			double supplied = result.suppliedPower[i];
			double load = result.loadCurve[i];
			if (supplied < load)
				diff += (load - supplied);
		}
		if (diff == 0)
			return;
		Item item = new Item();
		item.pos = -1;
		item.name = "Ungedeckte Leistung";
		item.heat = diff;
		items.add(item);
	}

	private class Item {
		int pos;
		String name;
		String power;
		String fuel;
		String rank;
		double heat;
		double share;
		Integer fullLoadHours;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		private ColorImage img = new ColorImage(UI.shell().getDisplay());

		@Override
		public Image getColumnImage(Object element, int col) {
			if (!(element instanceof Item) || col != 0)
				return null;
			Item item = (Item) element;
			return item.pos < 0 ? img.getRed() : img.get(item.pos);
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
				return item.power;
			case 2:
				return item.fuel;
			case 3:
				return item.rank;
			case 4:
				return Numbers.toString((int) item.heat) + " kWh";
			case 5:
				return Numbers.toString(item.share) + " %";
			case 6:
				return item.fullLoadHours == null ? null : Numbers
						.toString(item.fullLoadHours) + " h";
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
