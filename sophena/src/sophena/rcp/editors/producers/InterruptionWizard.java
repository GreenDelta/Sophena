package sophena.rcp.editors.producers;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import sophena.model.MonthDayHour;
import sophena.model.TimeInterval;
import sophena.rcp.M;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class InterruptionWizard extends Wizard {

	private Page page;
	private TimeInterval time;

	public static int open(TimeInterval time) {
		if (time == null)
			return Window.CANCEL;
		InterruptionWizard wiz = new InterruptionWizard();
		wiz.setWindowTitle("Betriebsunterbrechung");
		wiz.time = time;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		page.bindToModel();
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private MonthDayHourBox startBox;
		private MonthDayHourBox endBox;
		private Text descriptionText;

		private Page() {
			super("InterruptionWizardPage", "Betriebsunterbrechung", null);
			setDescription("Betriebsunterbrechung des Abnehmers");
		}

		@Override
		public void createControl(Composite parent) {
			UI.gridLayout(parent, 1);
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.innerGrid(comp, 1);

			Composite c1 = new Composite(comp, SWT.NONE);
			GridData gd1 = UI.gridData(c1, true, false);
			gd1.horizontalAlignment = GridData.END;
			c1.setLayoutData(gd1);
			GridLayout gl1 = UI.gridLayout(c1, 3);
			gl1.marginTop += 15;
			gl1.marginBottom = 10;
			c1.setLayout(gl1);
			startBox = new MonthDayHourBox(M.Start, c1);

			Composite c2 = new Composite(comp, SWT.NONE);
			GridData gd2 = UI.gridData(c2, true, false);
			gd2.horizontalAlignment = GridData.END;
			c2.setLayoutData(gd2);
			GridLayout gl2 = UI.gridLayout(c2, 3);
			gl2.marginBottom = 10;
			c2.setLayout(gl2);
			endBox = new MonthDayHourBox(M.End, c2);

			Composite c3 = new Composite(comp, SWT.NONE);
			GridData gd3 = UI.gridData(c3, true, false);
			GridLayout gl3 = UI.gridLayout(c3, 3);
			gl3.marginBottom += 20;
			gl3.horizontalSpacing += 7;
			gl3.verticalSpacing += 6;
			c3.setLayoutData(gd3);
			c3.setLayout(gl3);
			descriptionText = UI.formText(c3, M.Description);

			parent.getShell().pack();
			UI.center(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(),
					parent.getShell());
			bindToUI();
		}

		private void bindToUI() {
			if (time == null)
				return;
			setTime(time.start, startBox);
			setTime(time.end, endBox);
			Texts.set(descriptionText, time.description);
		}

		private void setTime(String time, MonthDayHourBox box) {
			if (time == null)
				return;
			try {
				box.select(MonthDayHour.parse(time));
			} catch (Exception e) {
				Log.error(this, "failed to parse MonthDayHour " + time, e);
			}
		}

		private void bindToModel() {
			time.start = startBox.getSelection().toString();
			time.end = endBox.getSelection().toString();
			time.description = descriptionText.getText();
		}
	}
}