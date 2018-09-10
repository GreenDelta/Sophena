package sophena.rcp.editors.results.compare;

import java.util.function.ToDoubleFunction;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.model.Project;
import sophena.rcp.utils.Colors;
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
		boolean withFunding = withFunding();
		if (withFunding) {
			CostTable.of(comparison).withFunding().render(body, tk);
		}
		CostTable.of(comparison).render(body, tk);
		InvestmentsTable.of(comparison).render(body, tk);
		KeyFigureTable.of(comparison).render(body, tk);
		simpleCostsChart("Investitionskosten", "EUR", v -> v.investments);

		// revenues charts
		simpleCostsChart("Wärmeerlöse", "EUR", v -> v.revenuesHeat);
		boolean withElectricityRevenues = false;
		for (ProjectResult r : comparison.results) {
			if (r.costResult.dynamicTotal.revenuesElectricity > 0) {
				withElectricityRevenues = true;
				break;
			}
		}
		if (withElectricityRevenues) {
			simpleCostsChart("Stromerlöse", "EUR", v -> v.revenuesElectricity);
		}

		simpleCostsChart("Jahresüberschuss", "EUR", v -> v.annualSurplus);
		heatCostsChart(withFunding);
		form.reflow(true);
	}

	private boolean withFunding() {
		for (ProjectResult r : comparison.results) {
			if (r.costResultFunding.dynamicTotal.funding > 0)
				return true;
		}
		return false;
	}

	private void simpleCostsChart(String title, String unit,
			ToDoubleFunction<CostResult.FieldSet> fn) {
		BarChart chart = BarChart.of(title).unit(unit);
		for (int i = 0; i < comparison.projects.length; i++) {
			Color color = Colors.getForChart(i);
			Project project = comparison.projects[i];
			CostResult r = comparison.results[i].costResult;
			double value = fn.applyAsDouble(r.dynamicTotal);
			chart.addBar(project.name, value, color);
		}
		chart.render(body, tk);
	}

	private void heatCostsChart(boolean withFunding) {
		BarChart chart = BarChart.of("Wärmegestehungskosten")
				.unit("EUR/MWh");
		for (int i = 0; i < comparison.projects.length; i++) {
			Color color = Colors.getForChart(i);
			Project project = comparison.projects[i];
			ProjectResult result = comparison.results[i];
			if (withFunding) {
				chart.addBar(project.name + " - mit Förderung",
						result.costResult.dynamicTotal.heatGenerationCosts,
						Colors.darker(color, 40));
			}
			String name = withFunding ? project.name + " - ohne Förderung"
					: project.name;
			chart.addBar(name,
					result.costResultFunding.dynamicTotal.heatGenerationCosts,
					color);
		}
		chart.render(body, tk);
	}

}
