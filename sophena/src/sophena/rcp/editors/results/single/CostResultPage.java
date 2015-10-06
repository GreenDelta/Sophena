package sophena.rcp.editors.results.single;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class CostResultPage extends FormPage {

	private ProjectResult result;

	public CostResultPage(ResultEditor editor) {
		super(editor, "sophena.CostResultPage", "Kosten");
		this.result = editor.result;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse - Kosten");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		fillSection(
				UI.formSection(body, tk, "Kosten ohne Förderung"),
				result.costResult);
		fillSection(
				UI.formSection(body, tk, "Kosten mit Förderung"),
				result.costResultFunding);
		form.reflow(true);
	}

	private void fillSection(Composite c, CostResult result) {
		TableViewer table = Tables.createViewer(c, "", "netto", "brutto");
		Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
		table.setLabelProvider(new Label());
		table.setInput(getItems(result));
		Table t = table.getTable();
		t.getColumn(1).setAlignment(SWT.RIGHT);
		t.getColumn(2).setAlignment(SWT.RIGHT);
	}

	private Item[] getItems(CostResult result) {
		CostResult.FieldSet netto = result.netto;
		CostResult.FieldSet brutto = result.brutto;
		return new Item[] {
				new Item("Investitionskosten",
						netto.investments,
						brutto.investments),
				new Item("Kapitalgebundene Kosten",
						netto.capitalCosts,
						brutto.capitalCosts),
				new Item("Bedarfsgebundene Kosten",
						netto.consumptionCosts,
						brutto.consumptionCosts),
				new Item("Betriebsgebundene Kosten",
						netto.operationCosts,
						brutto.operationCosts),
				new Item("Sonstige Kosten",
						netto.otherCosts,
						brutto.otherCosts),
				new Item("Erlöse",
						netto.revenues,
						brutto.revenues),
				new Item("Jährliche Kosten",
						netto.annualCosts,
						brutto.annualCosts),
				new Item("Wärmegestehungskosten",
						eur(netto.heatGenerationCosts * 1000) + "/MWh",
						eur(brutto.heatGenerationCosts * 1000) + "/MWh")
		};
	}

	private String eur(double value) {
		return Numbers.toString((int) Math.round(value)) + " EUR";
	}

	private class Item {

		final String label;
		final String netto;
		final String brutto;

		Item(String label, double netto, double brutto) {
			this.label = label;
			this.netto = eur(netto);
			this.brutto = eur(brutto);
		}

		Item(String label, String netto, String brutto) {
			this.label = label;
			this.netto = netto;
			this.brutto = brutto;
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
