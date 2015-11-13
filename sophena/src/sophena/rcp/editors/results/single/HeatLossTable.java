package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

import sophena.calc.EnergyResult;
import sophena.math.energetic.FuelEnergyDemand;
import sophena.math.energetic.HeatLoss;
import sophena.model.Project;
import sophena.rcp.utils.Tables;

public class HeatLossTable {

	private EnergyResult result;
	private Project project;

	private HeatLossTable(ResultEditor editor) {
		this.result = editor.result.energyResult;
		this.project = editor.project;
	}

	public static void create(ResultEditor editor, Composite comp) {
		new HeatLossTable(editor).render(comp);
	}

	private void render(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "",
				"Absolut [kWh]", "Prozentual [%]");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Table t = table.getTable();
		t.getColumn(1).setAlignment(SWT.RIGHT);
		t.getColumn(2).setAlignment(SWT.RIGHT);
		Tables.autoSizeColumns(t);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		Item fuelEnergyItem = new Item();
		fuelEnergyItem.label = "Brennstoffenergie";
		fuelEnergyItem.absolute = s(FuelEnergyDemand.getTotalKWh(result));
		items.add(fuelEnergyItem);

		Item conversionItem = new Item();
		conversionItem.label = "Konversionsverluste";
		conversionItem.absolute = s(HeatLoss.getAbsoluteConversionLossKWh(result));
		conversionItem.relative = s(100 * HeatLoss.getRelativeConversionLoss(result));
		items.add(conversionItem);

		Item heatItem = new Item();
		heatItem.label = "Energie nach Heizhaus";
		heatItem.absolute = s(result.totalProducedHeat);
		items.add(heatItem);

		Item netItem = new Item();
		netItem.label = "Verteilungsverluste";
		netItem.absolute = s(HeatLoss.getAbsoluteNetLossKWh(project));
		netItem.relative = s(100 * HeatLoss.getRelativeNetLoss(project, result));
		items.add(netItem);

		double afterNet = result.totalProducedHeat
				- HeatLoss.getAbsoluteNetLossKWh(project);
		Item consumerItem = new Item();
		consumerItem.label = "Energie nach Wärmenetz";
		consumerItem.absolute = s(afterNet);
		items.add(consumerItem);

		items.add(new Item());
		Item totalItem = new Item();
		totalItem.total = true;
		totalItem.label = "Gesamtverluste";
		totalItem.absolute = s(HeatLoss.getAbsoluteTotalLossKWh(project, result));
		totalItem.relative = s(100 * HeatLoss.getRelativeTotalLoss(project, result));
		items.add(totalItem);

		return items;
	}

	private String s(double d) {
		return Long.toString((int) Math.round(d));
	}

	private class Item {
		String label;
		String absolute;
		String relative;
		boolean total = false;
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
