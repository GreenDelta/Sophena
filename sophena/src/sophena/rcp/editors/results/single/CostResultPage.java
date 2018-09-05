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

import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class CostResultPage extends FormPage {

	private ProjectResult result;

	public CostResultPage(ResultEditor editor) {
		super(editor, "sophena.CostResultPage", "Wirtschaftlichkeit");
		this.result = editor.result;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Wirtschaftlichkeit");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		boolean withFunding = result.costResultFunding.netTotal.funding > 0;
		if (withFunding) {
			fillSection(UI.formSection(body, tk,
					"Wirtschaftlichkeit - mit Förderung"),
					result.costResultFunding, true);
			ProductAreaTable.create(result.costResultFunding,
					UI.formSection(body, tk,
							"Kostenübersicht - mit Förderung"));
			CostDetailsTable.create(result.costResultFunding,
					UI.formSection(body, tk, "Kostendetails - mit Förderung"));
		}
		fillSection(
				UI.formSection(body, tk, "Wirtschaftlichkeit - ohne Förderung"),
				result.costResult, false);
		ProductAreaTable.create(result.costResult,
				UI.formSection(body, tk,
						"Kostenübersicht - ohne Förderung"));
		CostDetailsTable.create(result.costResult,
				UI.formSection(body, tk, "Kostendetails - ohne Förderung"));
		form.reflow(true);
	}

	private void fillSection(Composite c, CostResult result,
			boolean withFunding) {
		TableViewer table = Tables.createViewer(c, "", "netto", "brutto");
		Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
		table.setLabelProvider(new Label());
		table.setInput(getItems(result, withFunding));
		Tables.rightAlignColumns(table, 1, 2);
	}

	private List<Item> getItems(CostResult result, boolean withFunding) {
		CostResult.FieldSet netto = result.netTotal;
		CostResult.FieldSet brutto = result.grossTotal;
		List<Item> items = new ArrayList<>();
		items.add(new Item("Investitionskosten", "EUR",
				netto.investments, brutto.investments));
		if (withFunding) {
			items.add(new Item("Investitionsförderung", "EUR",
					netto.funding, brutto.funding));
		}
		items.add(new Item("Kapitalgebundene Kosten", "EUR/a",
				netto.capitalCosts, brutto.capitalCosts));
		items.add(new Item("Bedarfsgebundene Kosten", "EUR/a",
				netto.consumptionCosts, brutto.consumptionCosts));
		items.add(new Item("Betriebsgebundene Kosten", "EUR/a",
				netto.operationCosts, brutto.operationCosts));
		items.add(new Item("Sonstige Kosten", "EUR/a",
				netto.otherCosts, brutto.otherCosts));
		items.add(new Item("Wärmeerlöse", "EUR/a",
				netto.revenuesHeat, brutto.revenuesHeat));
		if (netto.revenuesElectricity > 0) {
			items.add(new Item("Stromerlöse", "EUR/a",
					netto.revenuesElectricity, brutto.revenuesElectricity));
		}
		items.add(new Item("Kosten - Erlöse", "EUR/a",
				netto.annualCosts,
				brutto.annualCosts));
		items.add(new Item("Wärmegestehungskosten", "EUR/MWh",
				netto.heatGenerationCosts,
				brutto.heatGenerationCosts));
		return items;
	}

	private class Item {

		final String label;
		final String netto;
		final String brutto;

		Item(String label, String unit, double netto, double brutto) {
			this.label = label;
			this.netto = Num.intStr(Math.round(netto)) + " " + unit;
			this.brutto = Num.intStr(Math.round(brutto)) + " " + unit;
		}

	}

	private class Label extends LabelProvider implements ITableLabelProvider {

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
				return item.netto;
			case 2:
				return item.brutto;
			default:
				return null;
			}
		}
	}
}
