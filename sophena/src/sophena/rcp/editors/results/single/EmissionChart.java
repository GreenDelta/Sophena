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
		data.minimumHeight = 275;
		data.minimumWidth = 650;
		browser.setLayoutData(data);
		Chart chart = new Chart(browser);
		BarConfig config = new BarConfig();
		config.width = 600;
		config.height = 225;
		config.barValueSpacing = 50;
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
		ds.data.addAll(Arrays.asList(result.total, result.variantNaturalGas,
				result.variantOil));
		return data;
	}

}
