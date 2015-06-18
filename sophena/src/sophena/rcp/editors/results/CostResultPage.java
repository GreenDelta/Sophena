package sophena.rcp.editors.results;

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
import sophena.rcp.Numbers;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class CostResultPage extends FormPage {

	private CostResult result;

	public CostResultPage(ResultEditor editor) {
		super(editor, "sophena.CostResultPage", "Kosten");
		this.result = editor.result.costResult;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisse - Kosten");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite c = UI.formSection(body, tk, "Ergebnisübersicht");
		TableViewer table = Tables.createViewer(c, "", "netto", "brutto");
		Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
		table.setLabelProvider(new Label());
		table.setInput(getItems());
		form.reflow(true);
	}

	private Item[] getItems() {
		return new Item[] {
				new Item("Investitionskosten",
						result.netto.investments,
						result.brutto.investments),
				new Item("Kapitalgebundene Kosten (ohne Förderung)",
						result.netto.capitalCosts,
						result.brutto.capitalCosts),
				new Item("Kapitalgebundene Kosten (mit Förderung)",
						result.netto.capitalCostsFunding,
						result.brutto.capitalCostsFunding),
				new Item("Sonstige Kosten",
						result.netto.otherCosts,
						result.brutto.otherCosts)
		};
	}

	private class Item {

		final String label;
		final double netto;
		final double brutto;

		Item(String label, double netto, double brutto) {
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
				return Numbers.toString(item.netto) + " EUR";
			case 2:
				return Numbers.toString(item.brutto) + " EUR";
			default:
				return null;
			}
		}
	}
}
