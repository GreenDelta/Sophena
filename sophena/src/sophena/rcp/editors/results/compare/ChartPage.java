package sophena.rcp.editors.results.compare;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.Comparison;
import sophena.rcp.utils.UI;

class ChartPage extends FormPage {

	private Comparison comparison;

	ChartPage(ComparisonView view) {
		super(view, "ComparisonChartPage", "Ergebnisvergleich");
		this.comparison = view.comparison;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisvergleich");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		heatCostsSection(tk, body);

		Section section = UI.section(body, tk, "Investitionskosten");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);

	}

	private void heatCostsSection(FormToolkit tk, Composite body) {
		Section section = UI.section(body, tk, "Wärmegestehungskosten");
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		new HeatCostsChart(comparison).create(composite);
		new HeatCostsTable(comparison).create(composite);
	}

}
