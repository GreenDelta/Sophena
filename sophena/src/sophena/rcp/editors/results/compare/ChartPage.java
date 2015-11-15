package sophena.rcp.editors.results.compare;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
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
		annualCostsSection(tk, body);
		annualRevenuesSection(tk, body);
		investmentSection(tk, body);
		form.reflow(true);
	}

	private void heatCostsSection(FormToolkit tk, Composite body) {
		Section section = UI.section(body, tk, "Wärmegestehungskosten (netto)");
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1).verticalSpacing = 0;
		BarChart.create(comparison, composite, new BarChart.Data() {
			@Override
			public double value(CostResult result) {
				return getHeatCosts(result);
			}

			@Override
			public String unit() {
				return "EUR/MWh";
			}
		});
		ChartTable.create(comparison, composite, new ChartTable.Data() {
			@Override
			public double value(CostResult result) {
				return getHeatCosts(result);
			}

			@Override
			public String columnLabel() {
				return "Wärmegestehungskosten [EUR/MWh]";
			}
		});
	}

	private void annualCostsSection(FormToolkit tk, Composite body) {
		Section section = UI.section(body, tk, "Jährliche Kosten (netto)");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1).verticalSpacing = 0;
		BarChart.create(comparison, comp, new BarChart.Data() {
			@Override
			public double value(CostResult result) {
				return getAnnualCosts(result);
			}

			@Override
			public String unit() {
				return "EUR";
			}
		});
		ChartTable.create(comparison, comp, new ChartTable.Data() {

			@Override
			public double value(CostResult result) {
				return getAnnualCosts(result);
			}

			@Override
			public String columnLabel() {
				return "Jährliche Kosten [EUR]";
			}
		});
	}

	private void annualRevenuesSection(FormToolkit tk, Composite body) {
		Section section = UI.section(body, tk, "Jährliche Erlöse (netto)");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1).verticalSpacing = 0;
		BarChart.create(comparison, comp, new BarChart.Data() {
			@Override
			public double value(CostResult result) {
				return getAnnualRevenues(result);
			}

			@Override
			public String unit() {
				return "EUR";
			}
		});
		ChartTable.create(comparison, comp, new ChartTable.Data() {

			@Override
			public double value(CostResult result) {
				return getAnnualRevenues(result);
			}

			@Override
			public String columnLabel() {
				return "Jährliche Erlöse [EUR]";
			}
		});
	}

	private void investmentSection(FormToolkit tk, Composite body) {
		Section section = UI.section(body, tk, "Investitionskosten (netto)");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1).verticalSpacing = 0;
		BarChart.create(comparison, comp, new BarChart.Data() {
			@Override
			public double value(CostResult result) {
				return getInvestments(result);
			}

			@Override
			public String unit() {
				return "EUR";
			}
		});
		ChartTable.create(comparison, comp, new ChartTable.Data() {

			@Override
			public double value(CostResult result) {
				return getInvestments(result);
			}

			@Override
			public String columnLabel() {
				return "Investitionskosten [EUR]";
			}
		});
	}

	private double getHeatCosts(CostResult result) {
		if (result == null || result.netTotal == null)
			return 0;
		double val = result.netTotal.heatGenerationCosts;
		return val * 1000;
	}

	private double getInvestments(CostResult result) {
		if (result == null || result.netTotal == null)
			return 0;
		return result.netTotal.investments;
	}

	private double getAnnualCosts(CostResult result) {
		if (result == null || result.netTotal == null)
			return 0;
		return result.netTotal.annualCosts;
	}

	private double getAnnualRevenues(CostResult result) {
		if (result == null || result.netTotal == null)
			return 0;
		return result.netTotal.revenues;
	}

}
