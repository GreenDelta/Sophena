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
		CostDetailsTable.of(comparison).render(body, tk);
		KeyFigureTable.of(comparison).render(body, tk);
		simpleCostsChart("Investitionskosten", "EUR", v -> v.investments);
		simpleCostsChart("Erlöse", "EUR", v -> v.revenues);
		simpleCostsChart("Jährliche Kosten", "EUR", v -> v.annualCosts);
		heatCostsChart(withFunding);
		form.reflow(true);
	}

	private boolean withFunding() {
		for (Project project : comparison.projects) {
			// TODO: check for total funding
			if (project.costSettings != null
					&& project.costSettings.funding > 0)
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
			double value = fn.applyAsDouble(r.netTotal);
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
						1000 * result.costResult.netTotal.heatGenerationCosts,
						Colors.darker(color, 40));
			}
			String name = withFunding ? project.name + " - ohne Förderung"
					: project.name;
			chart.addBar(name,
					1000 * result.costResultFunding.netTotal.heatGenerationCosts,
					color);
		}
		chart.render(body, tk);
	}

}