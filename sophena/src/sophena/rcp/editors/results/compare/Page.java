package sophena.rcp.editors.results.compare;

import java.util.function.ToDoubleFunction;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.rcp.utils.UI;

class Page extends FormPage {

	private Comparison comparison;
	private FormToolkit tk;
	private Composite body;

	Page(ComparisonView view) {
		super(view, "ComparisonChartPage", "Ergebnisvergleich");
		this.comparison = view.comparison;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisvergleich");
		tk = mform.getToolkit();
		body = UI.formBody(form, tk);
		CostTable.of(comparison).render(body, tk);
		InvestmentsTable.of(comparison).render(body, tk);
		KeyFigureTable.of(comparison).render(body, tk);
		simpleCostsChart("Investitionskosten", "EUR", v -> v.investments);
		new CostsChart(comparison).render(body, tk);
		new RevenuesChart(comparison).render(body, tk);
		simpleCostsChart("Jahresüberschuss", "EUR", v -> v.annualSurplus);
		heatCostsChart();
		form.reflow(true);
	}

	private void simpleCostsChart(String title, String unit,
			ToDoubleFunction<CostResult.FieldSet> fn) {
		SimpleBarChart.of(title, comparison)
				.unit(unit)
				.data(r -> fn.applyAsDouble(r.costResultFunding.dynamicTotal))
				.render(body, tk);
	}

	private void heatCostsChart() {
		SimpleBarChart.of("Wärmegestehungskosten", comparison)
				.unit("EUR/MWh")
				.data(r -> r.costResultFunding.dynamicTotal.heatGenerationCosts)
				.render(body, tk);
	}

}
