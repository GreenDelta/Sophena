package sophena.rcp.wizards;

import java.io.File;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import sophena.io.HoursProfile;
import sophena.model.Consumer;
import sophena.model.Stats;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.UI;

public class LoadProfileWizard extends Wizard {

	private Page page;
	private Consumer consumer;

	public static void open(Consumer consumer) {
		if (consumer == null)
			return;
		LoadProfileWizard wiz = new LoadProfileWizard();
		wiz.setWindowTitle(M.LoadProfile);
		wiz.consumer = consumer;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		dialog.open();
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Canvas canvas;

		private Page() {
			super("LoadProfileWizardPage", M.LoadProfile, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 2);
			createNameText(composite);
			createDescriptionText(composite);
			createFileSection(composite);
			createChartCanvas(composite);
		}

		private void createNameText(Composite composite) {
			Text t = UI.formText(composite, M.Name);
			// TODO add data binding
		}

		private void createDescriptionText(Composite composite) {
			Text t = UI.formMultiText(composite, M.Description);
			// TODO add data binding
		}

		private void createFileSection(Composite composite) {
			UI.formLabel(composite, "");
			Button button = new Button(composite, SWT.NONE);
			button.setImage(Images.FILE_16.img());
			button.setText(M.SelectFile);
			Controls.onSelect(button, (e) -> {
				FileDialog dialog = new FileDialog(UI.shell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.txt", "*.csv" });
				dialog.setText(M.SelectFile);
				String path = dialog.open();
				if (path != null) {
					double[] data = HoursProfile.read(new File(path));
					createChart(data);
				}
			});
		}

		private void createChartCanvas(Composite composite) {
			UI.formLabel(composite, "");
			canvas = new Canvas(composite, SWT.NONE);
			UI.gridData(canvas, true, true);
		}

		private void createChart(double[] data) {
			LightweightSystem lws = new LightweightSystem(canvas);
			XYGraph g = new XYGraph();
			lws.setContents(g);
			g.setShowTitle(false);
			g.setShowLegend(false);
			CircularBufferDataProvider provider = new CircularBufferDataProvider(
					true);
			provider.setBufferSize(Stats.HOURS);
			provider.setCurrentYDataArray(data);
			Trace trace = new Trace("Data", g.primaryXAxis, g.primaryYAxis,
					provider);
			trace.setPointStyle(Trace.PointStyle.NONE);
			g.addTrace(trace);
			g.primaryXAxis.setVisible(false);
			g.primaryXAxis.setRange(0, Stats.HOURS);
			formatY(g, data);
		}

		private void formatY(XYGraph g, double[] data) {
			double max = Stats.max(data);
			max = Stats.nextStep(max, 5);
			Axis y = g.getYAxisList().get(0);
			y.setTitle("kW");
			y.setRange(0, max);
			y.setTitleFont(y.getFont());
		}
	}
}
