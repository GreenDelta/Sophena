package sophena.rcp.editors.results;

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
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.rcp.Numbers;
import sophena.rcp.utils.ColorImage;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class ElectricityResultPage extends FormPage {

	private EnergyResult result;

	public ElectricityResultPage(ResultEditor editor) {
		super(editor, "sophena.ElectricityResultPage", "Stromerzeugung");
		this.result = editor.result.energyResult;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse - Energie");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Section section = UI.section(body, tk, "Stromerzeugung");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		TableViewer table = Tables.createViewer(comp, "KWK-Anlage", "Rang",
				"el. Nennleistung", "Volllaststunden", "Erzeugter Strom");
		table.setLabelProvider(new Label());
		Tables.bindColumnWidths(table, 0.2, 0.2, 0.2, 0.2, 0.2);
		table.setInput(getItems());
		new ElectricityChart(result).render(body, tk);
		form.reflow(true);
	}

	private List<Item> getItems() {
		List<Item> list = new ArrayList<>();
		for (int i = 0; i < result.producers.length; i++) {
			Producer p = result.producers[i];
			if (p.boiler == null || !p.boiler.isCoGenPlant)
				continue;
			Item item = new Item();
			item.pos = i;
			list.add(item);
			item.name = p.name;
			if (p.function == ProducerFunction.BASE_LOAD)
				item.rank = p.rank + " - Grundlast";
			else
				item.rank = p.rank + " - Spitzenlast";
			item.maxPower = p.boiler.maxPowerElectric;
			double heat = result.totalHeat(p);
			item.fullLoadHours = getFullLoadHours(p, heat);
			item.value = item.fullLoadHours * item.maxPower;
		}
		return list;
	}

	private int getFullLoadHours(Producer p, double producedHeat) {
		if (p == null || p.boiler == null)
			return 0;
		double maxPower = p.boiler.maxPower;
		return (int) Math.round(producedHeat / maxPower);
	}

	private class Item {
		int pos;
		String name;
		String rank;
		double maxPower;
		double fullLoadHours;
		double value;
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
				return Numbers.toString(item.maxPower) + " kW";
			case 3:
				return Numbers.toString(item.fullLoadHours) + " h";
			case 4:
				return Numbers.toString(item.value) + " kWh";
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
