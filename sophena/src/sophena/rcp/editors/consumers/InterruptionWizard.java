package sophena.rcp.editors.consumers;

import java.time.MonthDay;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import sophena.model.TimeInterval;
import sophena.rcp.M;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.MonthDayBox;
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

		private MonthDayBox startBox;
		private MonthDayBox endBox;
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
			UI.gridLayout(c1, 2);
			startBox = new MonthDayBox(M.Start, c1);
			endBox = new MonthDayBox(M.End, c1);

			Composite c2 = new Composite(comp, SWT.NONE);
			UI.gridData(c2, true, false);
			UI.gridLayout(c2, 2);
			descriptionText = UI.formText(c2, M.Description);

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

		private void setTime(String time, MonthDayBox box) {
			if (time == null)
				return;
			try {
				MonthDay md = MonthDay.parse(time);
				box.select(md);
			} catch (Exception e) {
				Log.error(this, "failed to parse time " + time, e);
			}
		}

		private void bindToModel() {
			time.start = startBox.getSelection().toString();
			time.end = endBox.getSelection().toString();
			time.description = descriptionText.getText();
		}
	}
}
