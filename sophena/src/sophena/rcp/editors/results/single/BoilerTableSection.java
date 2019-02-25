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

import sophena.Labels;
import sophena.calc.EnergyResult;
import sophena.calc.ProjectResult;
import sophena.math.energetic.GeneratedHeat;
import sophena.math.energetic.Producers;
import sophena.math.energetic.UtilisationRate;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.rcp.M;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BoilerTableSection {

	private final ProjectResult projectResult;
	private final EnergyResult result;
	private final Project project;
	private final double maxLoad;

	BoilerTableSection(ResultEditor editor, double maxLoad) {
		this.projectResult = editor.result;
		this.result = editor.result.energyResult;
		this.project = editor.project;
		this.maxLoad = maxLoad;
	}

	public void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, M.HeatProducers);
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, M.HeatProducer, "Rang",
				"Nennleistung", "Brennstoffverbrauch", M.GeneratedHeat,
				"Anteil", "Volllaststunden", "Nutzungsgrad", "Starts");
		table.setLabelProvider(new Label());
		double w = 1.0 / 9.0;
		Tables.bindColumnWidths(table, w, w, w, w, w, w, w, w, w);
		Tables.rightAlignColumns(table, 2, 4, 5, 6, 7, 8);
		table.setInput(getItems());
	}

	private List<Item> getItems() {
		List<Item> items = new ArrayList<>();
		if (result.producers == null)
			return Collections.emptyList();
		initProducerItems(result.producers, items);
		double powerDiff = Producers.powerDifference(result.producers, maxLoad);
		addDiffItem(items, powerDiff);
		addBufferItem(items, result.producers);
		return items;
	}

	private void initProducerItems(Producer[] producers, List<Item> items) {
		for (int i = 0; i < producers.length; i++) {
			Producer p = producers[i];
			Item item = new Item();
			item.name = p.name;
			item.powerOrVolume = Num.intStr(Producers.maxPower(p)) + " kW";
			if (p.function == ProducerFunction.BASE_LOAD)
				item.rank = p.rank + " - Grundlast";
			else
				item.rank = p.rank + " - Spitzenlast";
			item.pos = i;
			double heat = result.totalHeat(p);
			item.fuelUse = Labels.getFuel(p) + ": "
					+ Num.intStr(projectResult.fuelUsage.getInFuelUnits(p))
					+ " " + Labels.getFuelUnit(p);
			item.producedHeat = Num.intStr(heat) + " kWh";
			item.share = GeneratedHeat.share(heat, result) + " %";
			item.fullLoadHours = (int) Producers.fullLoadHours(p, heat);
			if (p.boiler != null && p.boiler.isCoGenPlant) {
				item.utilisationRate = p.boiler.efficiencyRate;
			} else {
				item.utilisationRate = UtilisationRate.get(project, p, result);
			}
			item.clocks = result.numberOfStarts(p);
			items.add(item);
		}
	}

	private void addBufferItem(List<Item> items, Producer[] producers) {
		Item sep = new Item();
		sep.separator = true;
		items.add(sep);
		Item item = new Item();
		item.pos = producers.length;
		item.name = "Pufferspeicher";
		items.add(item);
		double heat = result.totalBufferedHeat;
		item.producedHeat = Num.intStr(heat) + " kWh";
		item.share = GeneratedHeat.share(heat, result) + " %";
		if (project.heatNet != null && project.heatNet.bufferTank != null) {
			double volume = project.heatNet.bufferTank.volume;
			item.powerOrVolume = Num.intStr(volume) + " L";
		}
	}

	private void addDiffItem(List<Item> items, double powerDiff) {
		double diff = 0;
		for (int i = 0; i < Stats.HOURS; i++) {
			double supplied = result.suppliedPower[i];
			double load = result.loadCurve[i];
			if (supplied < load)
				diff += (load - supplied);
		}
		if (diff < 0.5 && powerDiff >= 0)
			return;
		Item item = new Item();
		item.pos = -1;
		item.name = "Ungedeckte Leistung";
		item.powerOrVolume = powerDiff < 0
				? Num.intStr(powerDiff) + " kW"
				: null;
		if (diff >= 0.5) {
			item.producedHeat = "-" + Num.intStr(diff) + " kWh";
		}
		items.add(item);
	}

	private class Item {
		int pos;
		String name;
		String powerOrVolume;
		String fuelUse;
		String rank;
		String producedHeat;
		String share;
		Integer fullLoadHours;
		Double utilisationRate;
		Integer clocks;
		boolean separator = false;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		private ColorImage img = new ColorImage(UI.shell().getDisplay());

		@Override
		public Image getColumnImage(Object element, int col) {
			if (!(element instanceof Item) || col != 0)
				return null;
			Item item = (Item) element;
			if (item.separator)
				return null;
			return item.pos < 0 ? img.getRed() : img.get(item.pos);
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Item))
				return null;
			Item item = (Item) element;
			if (item.separator)
				return null;
			switch (col) {
			case 0:
				return item.name;
			case 1:
				return item.rank;
			case 2:
				return item.powerOrVolume;
			case 3:
				return item.fuelUse;
			case 4:
				return item.producedHeat;
			case 5:
				return item.share;
			case 6:
				return item.fullLoadHours == null ? null
						: Num.intStr(item.fullLoadHours) + " h";
			case 7:
				return item.utilisationRate == null ? null
						: Num.intStr(item.utilisationRate * 100) + " %";
			case 8:
				return item.clocks == null ? null
						: Num.intStr(item.clocks);
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
