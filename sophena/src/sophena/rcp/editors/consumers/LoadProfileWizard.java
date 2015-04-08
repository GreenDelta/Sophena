package sophena.rcp.editors.consumers;

import java.io.File;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import sophena.io.HoursProfile;
import sophena.model.Consumer;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.charts.LoadCurveChart;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.UI;

class LoadProfileWizard extends Wizard {

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

		private LoadCurveChart chart;

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
			createChart(composite);
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
				dialog.setFilterExtensions(new String[]{"*.txt", "*.csv"});
				dialog.setText(M.SelectFile);
				String path = dialog.open();
				if (path != null) {
					double[] data = HoursProfile.read(new File(path));
					chart.setData(data);
				}
			});
		}

		private void createChart(Composite composite) {
			UI.formLabel(composite, "");
			Composite chartParent = new Composite(composite, SWT.NONE);
			UI.gridData(chartParent, true, true);
			chartParent.setLayout(new FillLayout());
			chart = new LoadCurveChart(chartParent);
		}


	}
}
