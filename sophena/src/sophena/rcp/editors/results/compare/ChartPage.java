package sophena.rcp.editors.results.compare;

import java.util.function.ToDoubleFunction;

import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;
import sophena.rcp.charts.ChartImageExport;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.UI;

class ChartPage extends FormPage {

	private Comparison comparison;
	private FormToolkit tk;
	private Composite body;

	ChartPage(ComparisonView view) {
		super(view, "ComparisonChartPage", "Ergebnisvergleich");
		this.comparison = view.comparison;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Ergebnisvergleich");
		tk = mform.getToolkit();
		body = UI.formBody(form, tk);
		heatCostsSection();
		annualCostsSection();
		annualRevenuesSection();
		investmentSection();
		form.reflow(true);
	}

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
		Actions.bind(section, new ChartImageExport(title + ".jpg", () -> graph));
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
