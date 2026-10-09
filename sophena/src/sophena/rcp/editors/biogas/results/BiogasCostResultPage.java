package sophena.rcp.editors.biogas.results;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITableFontProvider;
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

import sophena.calc.biogas.BiogasCostCalculator;
import sophena.calc.costs.CostResult;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// Shows the dynamic and static cost result of a biogas plant.
class BiogasCostResultPage extends FormPage {

	private final BiogasPlantResultEditor editor;

	BiogasCostResultPage(BiogasPlantResultEditor editor) {
		super(editor, "sophena.BiogasCostResultPage", "Wirtschaftlichkeit");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var result = editor.result();
		var plant = result.plant();
		var form = UI.formHeader(mform, "Wirtschaftlichkeit");
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);

		if (plant != null && plant.settings != null) {
			var costs = new BiogasCostCalculator(plant, result).calculate();
			createCosts(body, tk, costs);
		}

		form.reflow(true);
	}

	private void createCosts(Composite body, FormToolkit tk, CostResult r) {
		var comp = UI.formSection(body, tk, "Wirtschaftlichkeit");
		TableViewer table = Tables.createViewer(comp, "", "dynamisch",
				"statisch");
		Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
		Tables.rightAlignColumns(table, 1, 2);
		table.setLabelProvider(new Label());
		table.setInput(itemsOf(r));
	}

	private static List<Item> itemsOf(CostResult r) {
		var dyn = r.dynamicTotal;
		var stat = r.staticTotal;
		List<Item> items = new ArrayList<>();

		// investment costs
		items.add(new Item("Investitionskosten", "EUR",
				dyn.investments, stat.investments));
		items.add(new Item("Investitionsförderung", "EUR",
				dyn.funding, stat.funding));
		items.add(new Item("Finanzierungsbedarf", "EUR",
				dyn.investments - dyn.funding,
				stat.investments - stat.funding).bold());
		items.add(new Item());

		// annual costs
		items.add(new Item("Kapitalgebundene Kosten", "EUR/a",
				dyn.capitalCosts, stat.capitalCosts));
		items.add(new Item("Bedarfsgebundene Kosten", "EUR/a",
				dyn.consumptionCosts, stat.consumptionCosts));
		items.add(new Item("Betriebsgebundene Kosten", "EUR/a",
				dyn.operationCosts, stat.operationCosts));
		items.add(new Item("Sonstige Kosten", "EUR/a",
				dyn.otherAnnualCosts, stat.otherAnnualCosts));
		items.add(new Item("Gesamtkosten", "EUR/a",
				dyn.totalAnnualCosts, stat.totalAnnualCosts).bold());

		return items;
	}

	private static class Item {

		String label;
		String netto;
		String brutto;
		boolean bold;

		Item() {
		}

		Item(String label, String unit, double netto, double brutto) {
			this.label = label;
			this.netto = Num.intStr(Math.round(netto)) + " " + unit;
			this.brutto = Num.intStr(Math.round(brutto)) + " " + unit;
		}

		Item bold() {
			bold = true;
			return this;
		}
	}

	private static class Label extends LabelProvider
			implements ITableLabelProvider, ITableFontProvider {

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof Item item))
				return null;
			return item.bold ? UI.boldFont() : null;
		}

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item item))
				return null;
			return switch (col) {
				case 0 -> item.label;
				case 1 -> item.netto;
				case 2 -> item.brutto;
				default -> null;
			};
		}
	}
}
