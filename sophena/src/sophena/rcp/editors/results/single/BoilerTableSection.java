package sophena.rcp.editors.results.single;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Color;
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
import sophena.model.Project;
import sophena.model.Stats;
import sophena.rcp.M;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.ResultColors;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BoilerTableSection {

	private final ProjectResult projectResult;
	private final EnergyResult result;
	private final ResultColors colors;
	private final Project project;
	private final double maxLoad;
	
	BoilerTableSection(ResultEditor editor, double maxLoad) {
		this.projectResult = editor.result;
		this.colors = editor.colors;
		this.result = editor.result.energyResult;
		this.project = editor.project;
		this.maxLoad = maxLoad;
	}

	public void render(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Wärmeerzeugung");
		UI.gridData(section, true, false);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);		
		List<Item> items = getItems();
		boolean showStagnationDays = false;
		for(int i = 0; i < items.size(); i++)
			if(items.get(i).showStagnationDays)
				showStagnationDays = true;
		
		boolean showJAZ = false;
		for(Producer p : result.producers)
			if(p.heatPump != null)
			{
				showJAZ = true;
				break;
			}				
		
		List<String> properties = new ArrayList<String>();
		properties.add(M.HeatProducer);
		properties.add("Rang");
		properties.add("Nennleistung");
		properties.add("Energieträgereinsatz");
		properties.add(M.GeneratedHeat);
		properties.add("Anteil");
		properties.add("Volllaststunden");
		properties.add("Nutzungsgrad");
		properties.add("Starts");
		properties.add("Stagnationstage");
		properties.add("JAZ");
		
		int count = 9;
		if(showStagnationDays)
			count++;
		if(showJAZ)
			count++;
		
		var table = Tables.createViewer(comp, properties.toArray(new String[0]));
		table.setLabelProvider(new Label());
		double w = 0.9 / count;
		if(count == 11)
			Tables.bindColumnWidths(table, w, w, w, w, w, w, w, w, w, w, w);		
		else if(count == 10)
		{
			if(showStagnationDays)
				Tables.bindColumnWidths(table, w, w, w, w, w, w, w, w, w, w, 0);
			else
				Tables.bindColumnWidths(table, w, w, w, w, w, w, w, w, w, 0, w);
		}
		else
			Tables.bindColumnWidths(table, w, w, w, w, w, w, w, w, w, 0, 0);
		Tables.rightAlignColumns(table, 2, 4, 5, 6, 7, 8, 9, 10, 11);		
		table.setInput(items);		
	}

	private List<Item> getItems() {
		if (result.producers == null)
			return Collections.emptyList();
		var items = new ArrayList<Item>();
		initProducerItems(result.producers, items);
		double powerDiff = Producers.powerDifference(result.producers, maxLoad);
		addDiffItem(items, powerDiff);
		addBufferItem(items);
		return items;
	}

	private void initProducerItems(Producer[] producers, List<Item> items) {
		for (Producer p : producers) {
			var item = new Item();
			item.name = p.name;
			double maxPower = p.solarCollector != null & p.solarCollectorSpec != null ? result.maxPeakPower(p) : Producers.maxPower(p);
			item.powerOrVolume = Num.intStr(maxPower) + " kW";
			item.rank = Labels.getRankText(p.function, p.rank);
			item.color = colors.of(p);
			double heat = result.totalHeat(p);
			item.fuelUse = Labels.getFuel(p) + ": "
					+ Num.intStr(projectResult.fuelUsage.getInFuelUnits(p))
					+ " " + Labels.getFuelUnit(p);
			item.producedHeat = Num.intStr(heat) + " kWh";
			item.share = GeneratedHeat.share(heat, result) + " %";
			item.fullLoadHours = maxPower == 0 ? 0 : (int) Math.ceil(heat / maxPower);;
			item.utilisationRate = p.boiler != null && p.boiler.isCoGenPlant
					? p.boiler.efficiencyRate
					: UtilisationRate.get(project, p, result);
			item.clocks = result.numberOfStarts(p);
			if(p.solarCollector != null & p.solarCollectorSpec != null) {				
				item.stagnationDays = result.stagnationDays(p);
				item.showStagnationDays = true;
			}
			if(p.heatPump != null)
			{
				item.utilisationRate = null;
				item.jaz = result.jaz(p);
				item.showJAZ = true;
			}
			items.add(item);
		}
	}
	
	private void addBufferItem(List<Item> items) {
		var sep = new Item();
		sep.separator = true;
		items.add(sep);
		var item = new Item();
		item.color = colors.of(ColorKey.BUFFER_TANK);
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

		var item = new Item();
		item.color = colors.of(ColorKey.UNCOVERED_LOAD);
		item.name = "Ungedeckte Leistung";
		item.powerOrVolume = powerDiff < 0
				? Num.intStr(powerDiff) + " kW"
				: null;
		if (diff >= 0.5) {
			item.producedHeat = "-" + Num.intStr(diff) + " kWh";
		}
		items.add(item);
	}

	private static class Item {
		Color color;
		String name;
		String powerOrVolume;
		String fuelUse;
		String rank;
		String producedHeat;
		String share;
		Integer fullLoadHours;
		Double utilisationRate;
		Integer clocks;
		Integer stagnationDays;
		boolean showStagnationDays;
		boolean separator = false;
		boolean showJAZ;
		Double jaz;
	}

	private static class Label extends LabelProvider
			implements ITableLabelProvider {

		private final ColorImage img = new ColorImage(UI.shell().getDisplay());

		@Override
		public Image getColumnImage(Object element, int col) {
			if (!(element instanceof Item item) || col != 0)
				return null;
			return item.separator
					? null
					: img.get(item.color);
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof Item item))
				return null;
			if (item.separator)
				return null;			
			return switch (col) {
				case 0 -> item.name;
				case 1 -> item.rank;
				case 2 -> item.powerOrVolume;
				case 3 -> item.fuelUse;
				case 4 -> item.producedHeat;
				case 5 -> item.share;
				case 6 -> item.fullLoadHours == null
						? null
						: Num.intStr(item.fullLoadHours) + " h";
				case 7 -> item.utilisationRate == null
						? null
						: Num.intStr(item.utilisationRate * 100) + " %";
				case 8 -> item.clocks == null
						? null
						: Num.intStr(item.clocks);
				case 9 -> item.stagnationDays == null 
						? null
						: Num.intStr(item.stagnationDays);
				case 10 -> item.jaz == null
						? null
						: new DecimalFormat("#0.0#").format(item.jaz);
				default -> null;
			};
		}

		@Override
		public void dispose() {
			img.dispose();
			super.dispose();
		}
	}
}
