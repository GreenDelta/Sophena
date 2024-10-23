package sophena.rcp.editors.results.single;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import sophena.Labels;
import sophena.calc.ProjectResult;
import sophena.math.energetic.GeneratedElectricity;
import sophena.math.energetic.Producers;
import sophena.rcp.M;
import sophena.rcp.colors.ResultColors;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Ref;

import java.util.ArrayList;
import java.util.List;

class ElectricityResultPage extends FormPage {

	private final ProjectResult result;
	private final ResultColors colors;

	ElectricityResultPage(ResultEditor editor) {
		super(editor, "sophena.ElectricityResultPage", M.Electricity);
		this.result = editor.result;
		this.colors = editor.colors;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, M.Electricity);
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);
		var section = UI.section(body, tk, M.ElectricityProducers);
		UI.gridData(section, true, false);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var table = Tables.createViewer(comp, "KWK-Anlage", "Rang",
				"Nennleistung", "Erzeugter Strom", "Anteil",
				"Volllaststunden", M.EfficiencyRate);
		table.setLabelProvider(new Label());
		double w = 1d / 7d;
		Tables.bindColumnWidths(table, w, w, w, w, w, w, w);
		Tables.rightAlignColumns(table, 2, 3, 4, 5, 6);
		table.setInput(getItems());
		new ElectricityChart(result.energyResult, colors)
				.render(body, tk);
		form.reflow(true);
	}

	private List<Item> getItems() {
		var r = result.energyResult;
		var list = new ArrayList<Item>();
		Ref<Double> total = Ref.of(0d);
		for (int i = 0; i < r.producers.length; i++) {
			var p = r.producers[i];
			double elPower = Producers.electricPower(p);
			if (elPower <= 0)
				continue;

			var item = new Item();
			item.color = colors.of(p);
			list.add(item);
			item.name = p.name;
			item.rank = Labels.getRankText(p.function, p.rank);
			item.maxPower = elPower;
			item.efficiencyRate = Producers.electricalEfficiency(p);
			item.fullLoadHours = Producers.fullLoadHours(p, result);
			item.value = GeneratedElectricity.get(p, result);
			total.set(total.get() + item.value);
		}
		if (total.get() != 0) {
			list.forEach(item -> item.share = item.value / total.get());
		}
		return list;
	}

	private static class Item {
		Color color;
		String name;
		String rank;
		double maxPower;
		double fullLoadHours;
		double value;
		Double efficiencyRate;
		double share;
	}

	private static class Label extends LabelProvider implements ITableLabelProvider {

		private final ColorImage img = new ColorImage(UI.shell().getDisplay());

		@Override
		public Image getColumnImage(Object o, int col) {
			return o instanceof Item item && col == 0
					? img.get(item.color)
					: null;
		}

		@Override
		public String getColumnText(Object o, int col) {
			if (!(o instanceof Item item))
				return null;
			return switch (col) {
				case 0 -> item.name;
				case 1 -> item.rank;
				case 2 -> Num.intStr(item.maxPower) + " kW";
				case 3 -> Num.intStr(item.value) + " kWh";
				case 4 -> Num.intStr(item.share * 100d) + " %";
				case 5 -> Num.intStr(item.fullLoadHours) + " h";
				case 6 -> item.efficiencyRate != null
						? Num.intStr(item.efficiencyRate * 100d) + " %"
						: null;
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
