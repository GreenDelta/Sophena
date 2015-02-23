package sophena.rcp.wizards;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.model.Project;
import sophena.rcp.M;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.UI;

public class ProducerWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Project project;

	public static void open(Project project) {
		ProducerWizard wiz = new ProducerWizard();
		wiz.setWindowTitle(M.CreateNewProducer);
		wiz.project = project;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		if (dialog.open() == Window.OK)
			Navigator.refresh();
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
			super("ProducerWizardPage", M.CreateNewProducer, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			createComboGroup(root);
			createList(root);
			createFunctionFields(root);
		}

		private void createComboGroup(Composite root) {
			Composite composite = UI.formComposite(root);
			UI.gridData(composite, true, false);
			UI.formCombo(composite, "#Erzeugertyp");
			UI.formCombo(composite, "#Anlagengröße");
		}

		private void createList(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridData(composite, true, true);
			UI.gridLayout(composite, 1);
			List list = new List(composite, SWT.BORDER);
			UI.gridData(list, true, true);
		}

		private void createFunctionFields(Composite root) {
			Composite composite = UI.formComposite(root);
			UI.gridData(composite, true, false);
			UI.formText(composite, "#Rang");
			UI.formCombo(composite, "#Funktion");
		}
	}
}
