package sophena.rcp.editors.results.single;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import chart.BarConfig;
import chart.BarData;
import chart.BarDataSet;
import chart.Chart;
import sophena.math.energetic.CO2Emissions;

class EmissionChart {

	private CO2Emissions result;

	private EmissionChart(CO2Emissions result) {
		this.result = result;
	}

	public static void create(CO2Emissions result, Composite comp) {
		new EmissionChart(result).render(comp);
	}

	private void render(Composite comp) {
		Browser browser = new Browser(comp, SWT.NONE);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
		data.minimumHeight = 250;
		data.minimumWidth = 650;
		browser.setLayoutData(data);
		Chart chart = new Chart(browser);
		BarConfig config = new BarConfig();
		config.width = 600;
		config.height = 220;
		config.barValueSpacing = 50;
		config.scaleBeginAtZero = true;
		chart.bar(makeData(), config);
	}

	private BarData makeData() {
		BarData data = new BarData();
		data.labels.addAll(Arrays.asList(
				"Emissionen Wärmenetz", "Emissionen Erdgas", "Emissionen Heizöl"));
		BarDataSet ds = new BarDataSet();
		data.datasets.add(ds);
		ds.label = "Emissionen";
		ds.fillColor = "rgba(151,187,205,0.5)";
		ds.strokeColor = "rgba(151,187,205,0.8)";
		ds.highlightFill = "rgba(151,187,205,0.75)";
		ds.highlightStroke = "rgba(151,187,205,1)";
		ds.data.addAll(Arrays.asList(
				(double) Math.round(result.total),
				(double) Math.round(result.variantNaturalGas),
				(double) Math.round(result.variantOil)));
		return data;
	}

}
