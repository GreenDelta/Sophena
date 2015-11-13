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
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.EnergyResult;
import sophena.math.energetic.CO2Emissions;
import sophena.model.Producer;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class EmissionsPage extends FormPage {

	private EnergyResult result;

	EmissionsPage(ResultEditor editor) {
		super(editor, "sophena.EmissionsPage", "Emissionen und Verluste");
		this.result = editor.result.energyResult;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Emissionen und Verluste");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite comp = UI.formSection(body, tk, "Treibhausgasemissionen");
		TableViewer table = Tables.createViewer(comp, "Wärmeerzeuger",
				"Emissionen [kg CO2 äq.]");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Table t = table.getTable();
		t.getColumn(1).setAlignment(SWT.RIGHT);
		Tables.autoSizeColumns(t);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<>();
		for (Producer p : result.producers) {
			Item item = new Item();
			item.label = p.name;
			item.co2(CO2Emissions.getKg(p, result.totalHeat(p)));
			items.add(item);
		}
		addCreditsItem(items);
		Item totalItem = new Item();
		totalItem.label = "Emissionen Wärmenetz";
		totalItem.co2(CO2Emissions.getTotalWithCreditsKg(result));
		totalItem.total = true;
		items.add(new Item());
		items.add(totalItem);
		return items;
	}

	private void addCreditsItem(List<Item> items) {
		double credits = CO2Emissions.getElectricityCreditsKg(result);
		if (credits > 0) {
			Item creditItem = new Item();
			creditItem.label = "Gutschrift Stromerzeugung";
			creditItem.co2(-credits);
			items.add(creditItem);
		}
	}

	private class Item {
		String label;
		String emissions;
		boolean total = false;

		void co2(double value) {
			emissions = Integer.toString((int) value);
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
				return item.emissions;
			default:
				return null;
			}
		}

	}

}
