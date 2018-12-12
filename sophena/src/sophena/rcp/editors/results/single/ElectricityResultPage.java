package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.EnergyResult;
import sophena.calc.ProjectResult;
import sophena.math.energetic.GeneratedElectricity;
import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.rcp.M;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Ref;

class ElectricityResultPage extends FormPage {

	private ProjectResult result;

	ElectricityResultPage(ResultEditor editor) {
		super(editor, "sophena.ElectricityResultPage", M.Electricity);
		this.result = editor.result;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Electricity);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Section section = UI.section(body, tk, M.ElectricityProducers);
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, "KWK-Anlage", "Rang",
				"Nennleistung", "Erzeugter Strom", "Anteil",
				"Volllaststunden", M.EfficiencyRate);
		table.setLabelProvider(new Label());
		double w = 1d / 7d;
		Tables.bindColumnWidths(table, w, w, w, w, w, w, w);
		Tables.rightAlignColumns(table, 2, 3, 4, 5, 6);
		table.setInput(getItems());
		new ElectricityChart(result.energyResult).render(body, tk);
		form.reflow(true);
	}

	private List<Item> getItems() {
		EnergyResult r = result.energyResult;
		List<Item> list = new ArrayList<>();
		Ref<Double> total = Ref.of(0d);
		for (int i = 0; i < r.producers.length; i++) {
			Producer p = r.producers[i];
			double elPower = Producers.electricPower(p);
			if (elPower <= 0)
				continue;
			Item item = new Item();
			item.pos = i;
			list.add(item);
			item.name = p.name;
			if (p.function == ProducerFunction.BASE_LOAD) {
				item.rank = p.rank + " - Grundlast";
			} else {
				item.rank = p.rank + " - Spitzenlast";
			}
			item.maxPower = elPower;
			item.efficiencyRate = Producers.electricalEfficiency(p);
			item.fullLoadHours = Producers.fullLoadHours(p, result);
			item.value = GeneratedElectricity.get(p, result);
			total.set(total.get() + item.value);
		}
		if (total.get() != 0)
			list.forEach(item -> item.share = item.value / total.get());
		return list;
	}

	private class Item {
		int pos;
		String name;
		String rank;
		double maxPower;
		double fullLoadHours;
		double value;
		Double efficiencyRate;
		double share;
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
				return item.rank;
			case 2:
				return Num.intStr(item.maxPower) + " kW";
			case 3:
				return Num.intStr(item.value) + " kWh";
			case 4:
				return Num.intStr(item.share * 100d) + " %";
			case 5:
				return Num.intStr(item.fullLoadHours) + " h";
			case 6:
				if (item.efficiencyRate != null)
					return Num.intStr(item.efficiencyRate * 100d) + " %";
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
