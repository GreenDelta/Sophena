package sophena.rcp.editors;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.io.HoursProfile;
import sophena.rcp.Images;
import sophena.rcp.charts.LoadCurveChart;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.UI;

public class LoadCurveSection {

	private LoadCurveChart chart;
	private boolean sorted = true;
	private double[] rawData;
	private double[] chartData;

	public LoadCurveSection(Composite body, FormToolkit tk) {
		render(body, tk);
	}

	public void setSorted(boolean sorted) {
		this.sorted = sorted;
	}

	public void setData(double[] data) {
		if (data == null)
			return;
		rawData = data;
		chartData = sorted ? getSortedCopy(rawData) : rawData;
		chart.setData(chartData);
	}

	private double[] getSortedCopy(double[] rawData) {
		double[] copy = Arrays.copyOf(rawData, rawData.length);
		Arrays.sort(copy);
		for (int i = 0; i < copy.length / 2; i++) {
			int j = copy.length - i - 1;
			double v = copy[i];
			copy[i] = copy[j];
			copy[j] = v;
		}
		return copy;
	}

	private void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Jahresdauerlinie");
		GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
		section.setLayoutData(gridData);
		gridData.heightHint = 250;
		gridData.minimumHeight = 250;
		Composite composite = UI.sectionClient(section, tk);
		composite.setLayout(new FillLayout());
		chart = new LoadCurveChart(composite);
		Actions.bind(section, new SortAction(), new ExportAction());
	}

	private class SortAction extends Action {

		public SortAction() {
			setText("Unsortiert");
			setImageDescriptor(Images.SORTING_16.des());
		}

		@Override
		public void run() {
			if (sorted) {
				sorted = false;
				setText("Sortiert");
			} else {
				sorted = true;
				setText("Unsortiert");
			}
			setData(rawData);
		}
	}

	private class ExportAction extends Action {

		public ExportAction() {
			setText("Als Datei speichern");
			setImageDescriptor(Images.FILE_16.des());
		}

		@Override
		public void run() {
			FileDialog dialog = new FileDialog(UI.shell(), SWT.SAVE);
			dialog.setFilterExtensions(new String[] { "*.csv" });
			dialog.setText("Jahresdauerlinie speichern");
			String path = dialog.open();
			if (path != null) {
				Rcp.run("Exportiere...", () -> doExport(path));
			}
		}

		private void doExport(String path) {
			try {
				File file = new File(path);
				HoursProfile.write(rawData, file);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to export load profile to " + path, e);
			}
		}
	}
}
