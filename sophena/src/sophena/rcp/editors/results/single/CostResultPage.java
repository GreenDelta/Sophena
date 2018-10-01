package sophena.rcp.editors.results.single;

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
		boolean withFunding = result.costResultFunding.dynamicTotal.funding > 0;
		if (withFunding) {
			sections(body, tk, true);
		}
		sections(body, tk, false);
		form.reflow(true);
	}

	private void sections(Composite body, FormToolkit tk, boolean withFunding) {
		String suffix = withFunding ? "" : " - ohne Förderung";
		CostResult r = withFunding
				? result.costResultFunding
				: result.costResult;
		Composite comp = UI.formSection(body, tk,
				"Wirtschaftlichkeit" + suffix);
		TableViewer table = Tables.createViewer(comp, "", "dynamisch",
				"statisch");
		Tables.bindColumnWidths(table, 0.6, 0.2, 0.2);
		table.setLabelProvider(new Label());
		table.setInput(getItems(r, withFunding));
		Tables.rightAlignColumns(table, 1, 2);
		if (withFunding) {
			ProductAreaTable.create(r,
					UI.formSection(body, tk, "Kostenübersicht" + suffix));
			CostDetailsTable.create(r,
					UI.formSection(body, tk, "Kostendetails" + suffix));
		}
	}

	private List<Item> getItems(CostResult r, boolean withFunding) {
		CostResult.FieldSet dyn = r.dynamicTotal;
		CostResult.FieldSet stat = r.staticTotal;
		List<Item> items = new ArrayList<>();

		// investment costs
		items.add(new Item("Investitionskosten", "EUR",
				dyn.investments, stat.investments));
		if (withFunding) {
			items.add(new Item("Investitionsförderung", "EUR",
					dyn.funding, stat.funding));
		}
		double conFees = result.project.costSettings.connectionFees;
		items.add(new Item("Anschlusskostenbeiträge", "EUR", conFees, conFees));
		Item invSum = new Item("Finanzierungsbedarf", "EUR",
				dyn.investments - dyn.funding - conFees,
				stat.investments - stat.funding - conFees);
		invSum.bold = true;
		items.add(invSum);
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
		Item acSum = new Item("Gesamtkosten", "EUR/a",
				dyn.totalAnnualCosts, stat.totalAnnualCosts);
		items.add(acSum.bold());
		items.add(new Item());

		// revenues
		items.add(new Item("Wärmeerlöse", "EUR/a",
				dyn.revenuesHeat, stat.revenuesHeat));
		items.add(new Item("Stromerlöse", "EUR/a",
				dyn.revenuesElectricity, stat.revenuesElectricity));
		Item revSum = new Item("Gesamterlöse", "EUR/a",
				dyn.revenuesHeat + dyn.revenuesElectricity,
				stat.revenuesHeat + stat.revenuesElectricity);
		items.add(revSum.bold());
		items.add(new Item());

		items.add(new Item("Jahresüberschuss", "EUR/a",
				dyn.annualSurplus,
				stat.annualSurplus).bold());
		items.add(new Item("Wärmegestehungskosten", "EUR/MWh",
				dyn.heatGenerationCosts,
				stat.heatGenerationCosts).bold());
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

	private class Label extends LabelProvider
			implements ITableLabelProvider, ITableFontProvider {

		@Override
		public Font getFont(Object obj, int col) {
			if (!(obj instanceof Item))
				return null;
			Item i = (Item) obj;
			if (i.bold)
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
				return item.netto;
			case 2:
				return item.brutto;
			default:
				return null;
			}
		}
	}
}
