package sophena.rcp.editors;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.io.LoadProfileWriter;
import sophena.math.LoadSorting;
import sophena.model.LoadProfile;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.charts.LoadProfileChart;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.UI;

/**
 * Section that contains a load profile chart.
 */
public class LoadCurveSection {

	private LoadProfileChart chart;
	private boolean sorted = true;
	private String title = "Jahresdauerlinie";
	private LoadProfile rawData;
	private LoadProfile chartData;

	public void setSorted(boolean sorted) {
		this.sorted = sorted;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setData(LoadProfile data) {
		if (data == null)
			return;
		rawData = data;
		chartData = sorted ? LoadSorting.sort(rawData) : rawData;
		if (chart == null)
			return;
		chart.setData(chartData);
	}

	public void render(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, title);
		UI.gridData(section, true, false);
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1);
		chart = new LoadProfileChart(composite, 250);
		Actions.bind(section, new SortAction(), new ExportAction(),
				ImageExport.forXYGraph("Jahresdauerlinie.jpg", () -> chart.graph));
		if (chartData != null)
			chart.setData(chartData);
	}

	private class SortAction extends Action {

		public SortAction() {
			setText("Unsortiert");
			setImageDescriptor(Icon.SORTING_16.des());
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
			setText(M.SaveAsFile);
			setImageDescriptor(Icon.EXPORT_FILE_16.des());
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
				LoadProfileWriter writer = new LoadProfileWriter();
				writer.write(rawData, file);
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(getClass());
				log.error("failed to export load profile to " + path, e);
			}
		}
	}
}
