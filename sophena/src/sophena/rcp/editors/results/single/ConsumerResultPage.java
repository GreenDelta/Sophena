package sophena.rcp.editors.results.single;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.ConsumerResult;
import sophena.calc.ProjectLoad;
import sophena.calc.ProjectResult;
import sophena.model.Project;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class ConsumerResultPage extends FormPage {

	private ProjectResult result;
	private Project project;

	ConsumerResultPage(ResultEditor editor) {
		super(editor, "sophena.ConsumerResultPage", "Abnehmer");
		this.result = editor.result;
		this.project = editor.project;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Abnehmer");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Section section = UI.section(body, tk, "Übersicht Abnehmer");
		UI.gridData(section, true, true);
		Composite comp = UI.sectionClient(section, tk);
		TableViewer table = Tables.createViewer(comp, "Name", "Heizlast",
				"Wärmebedarf");
		table.setLabelProvider(new Label());
		table.setInput(createItems());
		Tables.rightAlignColumns(table, 1, 2);
		Tables.autoSizeColumns(table);
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<Item>();
		double totalDemand = 0;
		double totalLoad = 0;
		for (ConsumerResult cr : result.consumerResults) {
			if (cr.consumer == null)
				continue;
			Item item = new Item();
			item.label = cr.consumer.name;
			item.heatingLoad = cr.consumer.heatingLoad;
			item.heatDemand = cr.heatDemand;
			items.add(item);
			totalDemand += cr.heatDemand;
			totalLoad += item.heatingLoad;
		}
		Collections.sort(items, (i1, i2) -> Strings.compare(i1.label, i2.label));
		Item netItem = addNetItem(items);
		Item totalItem = new Item();
		totalItem.isTotal = true;
		totalItem.label = "Summe";
		totalItem.heatDemand = totalDemand + result.heatNetLoss;
		totalItem.heatingLoad = totalLoad + netItem.heatingLoad;
		items.add(totalItem);
		return items;
	}

	private Item addNetItem(List<Item> items) {
		Item netItem = new Item();
		netItem.label = "Netzverluste";
		netItem.heatDemand = result.heatNetLoss;
		netItem.heatingLoad = ProjectLoad.getNetLoad(project.heatNet);
		items.add(netItem);
		return netItem;
	}

	private class Item {
		String label;
		double heatDemand;
		double heatingLoad;
		boolean isTotal;
	}

	private class Label extends LabelProvider implements ITableLabelProvider,
			IFontProvider {

		@Override
		public Font getFont(Object obj) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			if (item.isTotal)
				return UI.boldFont();
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
				return Num.intStr(item.heatingLoad) + " kW";
			case 2:
				return Num.intStr(item.heatDemand) + " kWh";
			default:
				return null;
			}
		}

	}
}
