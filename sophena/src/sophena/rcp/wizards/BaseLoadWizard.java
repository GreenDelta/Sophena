package sophena.rcp.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Consumer;
import sophena.rcp.M;
import sophena.rcp.utils.UI;

public class BaseLoadWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Consumer consumer;

	public static void open(Consumer consumer) {
		if (consumer == null)
			return;
		BaseLoadWizard wiz = new BaseLoadWizard();
		wiz.setWindowTitle(M.BaseLoad);
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

		private Page() {
			super("BaseLoadWizardPage", M.BaseLoad, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createNameText(composite);
			createDescriptionText(composite);
			createPowerText(composite);
			createStart(composite);
			createEnd(composite);
		}

		private void createNameText(Composite composite) {
			Text t = UI.formText(composite, M.Name);
			// TODO add data binding
			UI.formLabel(composite, "");
		}

		private void createDescriptionText(Composite composite) {
			Text t = UI.formMultiText(composite, M.Description);
			// TODO add data binding
			UI.formLabel(composite, "");
		}

		private void createPowerText(Composite composite) {
			Text t = UI.formText(composite, M.Power);
			// TODO add data binding
			UI.formLabel(composite, "kW");
		}

		private void createStart(Composite composite) {
			UI.formLabel(composite, M.Start);
			DateTime dt = new DateTime(composite, SWT.DATE | SWT.DROP_DOWN);
			// TODO add data binding
			UI.formLabel(composite, "");
		}

		private void createEnd(Composite composite) {
			UI.formLabel(composite, M.End);
			DateTime dt = new DateTime(composite, SWT.DATE | SWT.DROP_DOWN);
			// TODO add data binding
			UI.formLabel(composite, "");
		}

	}
}