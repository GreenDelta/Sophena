package sophena.rcp.editors.results.compare;

import java.util.function.ToDoubleFunction;

import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.model.Project;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.utils.Actions;
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
		simpleCostsChart("Kosten - Erlöse", "EUR", v -> v.annualCosts);
		heatCostsChart(withFunding);

		heatCostsSection();
		annualCostsSection();
		annualRevenuesSection();
		investmentSection();
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
		BarChart2 chart = BarChart2.of(title).unit(unit);
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
		BarChart2 chart = BarChart2.of("Wärmegestehungskosten")
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

	// -> del

	private void heatCostsSection() {
		makeSection("Wärmegestehungskosten", "EUR/MWh",
				result -> {
					if (result == null || result.netTotal == null)
						return 0;
					double val = result.netTotal.heatGenerationCosts;
					return val * 1000;
				});
	}

	private void annualCostsSection() {
		makeSection("Jährliche Kosten", "EUR",
				result -> {
					if (result == null || result.netTotal == null)
						return 0;
					return result.netTotal.annualCosts;
				});
	}

	private void annualRevenuesSection() {
		ToDoubleFunction<CostResult> fn = result -> {
			if (result == null || result.netTotal == null)
				return 0;
			return result.netTotal.revenues;
		};
		double max = 0;
		for (ProjectResult result : comparison.results) {
			max = Math.max(max, fn.applyAsDouble(result.costResult));
			max = Math.max(max, fn.applyAsDouble(result.costResultFunding));
		}
		if (max == 0)
			return;
		makeSection("Jährliche Erlöse", "EUR", fn);
	}

	private void investmentSection() {
		makeSection("Investitionskosten", "EUR",
				result -> {
					if (result == null || result.netTotal == null)
						return 0;
					return result.netTotal.investments;
				});
	}

	private void makeSection(String title, String unit,
			ToDoubleFunction<CostResult> fn) {
		Section section = UI.section(body, tk, title);
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1).verticalSpacing = 0;
		XYGraph graph = createBarChart(composite, unit, fn);
		Actions.bind(section, ImageExport.forXYGraph(title + ".jpg",
				() -> graph));
		ChartTable.create(comparison, composite, new ChartTable.Data() {
			@Override
			public double value(CostResult result) {
				return fn.applyAsDouble(result);
			}

			@Override
			public String columnLabel() {
				return title + " [" + unit + "]";
			}
		});
	}

	private XYGraph createBarChart(Composite composite, String unit,
			ToDoubleFunction<CostResult> fn) {
		return BarChart.create(comparison, composite, new BarChart.Data() {
			@Override
			public double value(CostResult result) {
				return fn.applyAsDouble(result);
			}

			@Override
			public String unit() {
				return unit;
			}
		});
	}

}
