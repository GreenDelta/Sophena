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
import sophena.calc.EnergyResult;
import sophena.calc.ProjectLoad;
import sophena.calc.ProjectResult;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.TableClipboard;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Strings;

class ConsumerResultPage extends FormPage {

	private ProjectResult result;

	ConsumerResultPage(ResultEditor editor) {
		super(editor, "sophena.ConsumerResultPage", "Abnehmer");
		this.result = editor.result;
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
		Actions.bind(table, TableClipboard.onCopy(table));
	}

	private List<Item> createItems() {
		List<Item> items = new ArrayList<Item>();
		for (ConsumerResult r : result.consumerResults) {
			items.add(Item.of(r));
		}
		Collections.sort(items,
				(i1, i2) -> Strings.compare(i1.label, i2.label));
		items.add(Item.ofNetLoss(result));
		items.add(Item.ofBufferLoss(result.energyResult));
		items.add(new Item()); // empty row
		items.add(Item.sum(result));
		return items;
	}

	private static class Item {
		String label;
		String demand;
		String load;
		boolean isTotal;

		static Item of(ConsumerResult r) {
			Item item = new Item();
			if (r == null)
				return item;
			if (r.consumer != null) {
				item.label = r.consumer.name;
				item.load = f(r.consumer.heatingLoad, "kW");
			}
			item.demand = f(r.heatDemand, "kWh");
			return item;
		}

		static Item ofBufferLoss(EnergyResult r) {
			Item item = new Item();
			item.label = "Pufferspeicherverluste";
			if (r == null)
				return item;
			item.demand = f(r.totalBufferLoss, "kWh");
			return item;
		}

		static Item ofNetLoss(ProjectResult r) {
			Item item = new Item();
			item.label = "Netzverluste";
			if (r.energyResult == null || r.project == null)
				return item;
			item.demand = f(r.energyResult.heatNetLoss, "kWh");
			item.load = f(ProjectLoad.getMaxNetLoad(r.project), "kW");
			return item;
		}

		static Item sum(ProjectResult r) {
			Item item = new Item();
			item.label = "Summe";
			item.isTotal = true;
			double demand = 0;
			double load = 0;
			for (ConsumerResult cr : r.consumerResults) {
				demand += cr.heatDemand;
				if (cr.consumer != null) {
					load += cr.consumer.heatingLoad;
				}
			}
			if (r.energyResult != null) {
				demand += r.energyResult.heatNetLoss;
				demand += r.energyResult.totalBufferLoss;
			}
			if (r.project != null) {
				load += ProjectLoad.getMaxNetLoad(r.project);
			}
			item.demand = f(demand, "kWh");
			item.load = f(load, "kW");
			return item;
		}

		static String f(double value, String unit) {
			return Num.intStr(value) + " " + unit;
		}

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
				return item.load;
			case 2:
				return item.demand;
			default:
				return null;
			}
		}
	}
}
