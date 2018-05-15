package sophena.rcp.editors.results.single;

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
import sophena.model.Project;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class CostResultPage extends FormPage {

	private ProjectResult result;
	private Project project;

	public CostResultPage(ResultEditor editor) {
		super(editor, "sophena.CostResultPage", "Wirtschaftlichkeit");
		this.result = editor.result;
		this.project = editor.project;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Wirtschaftlichkeit");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		// TODO: check for total funding
		boolean withFunding = project.costSettings != null
				&& project.costSettings.funding > 0;
		if (withFunding) {
			fillSection(
					UI.formSection(body, tk,
							"Wirtschaftlichkeit - mit Förderung"),
					result.costResultFunding);
			CostDetailsTable.create(result.costResultFunding,
					UI.formSection(body, tk, "Kostendetails - mit Förderung"));
		}
		fillSection(
				UI.formSection(body, tk, "Wirtschaftlichkeit - ohne Förderung"),
				result.costResult);
		CostDetailsTable.create(result.costResult,
				UI.formSection(body, tk, "Kostendetails - ohne Förderung"));
		form.reflow(true);
	}

	private void fillSection(Composite c, CostResult result) {
		TableViewer table = Tables.createViewer(c, "", "netto", "brutto");
		Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
		table.setLabelProvider(new Label());
		table.setInput(getItems(result));
		Tables.rightAlignColumns(table, 1, 2);
	}

	private Item[] getItems(CostResult result) {
		CostResult.FieldSet netto = result.netTotal;
		CostResult.FieldSet brutto = result.grossTotal;
		return new Item[] {
				new Item("Investitionskosten", "EUR",
						netto.investments,
						brutto.investments),
				new Item("Kapitalgebundene Kosten", "EUR/a",
						netto.capitalCosts,
						brutto.capitalCosts),
				new Item("Bedarfsgebundene Kosten", "EUR/a",
						netto.consumptionCosts,
						brutto.consumptionCosts),
				new Item("Betriebsgebundene Kosten", "EUR/a",
						netto.operationCosts,
						brutto.operationCosts),
				new Item("Sonstige Kosten", "EUR/a",
						netto.otherCosts,
						brutto.otherCosts),
				new Item("Stromerlöse", "EUR/a",
						netto.revenues,
						brutto.revenues),
				new Item("Kosten - Erlöse", "EUR/a",
						netto.annualCosts,
						brutto.annualCosts),
				new Item("Wärmegestehungskosten", "EUR/MWh",
						netto.heatGenerationCosts * 1000,
						brutto.heatGenerationCosts * 1000)
		};
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
