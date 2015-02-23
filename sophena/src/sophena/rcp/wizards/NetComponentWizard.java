package sophena.rcp.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Project;
import sophena.rcp.utils.UI;

public class NetComponentWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Project project;

	public static void open(Project project) {
		try {
			NetComponentWizard wiz = new NetComponentWizard();
			wiz.setWindowTitle("#Neue Komponente");
			wiz.project = project;
			WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
			dialog.setPageSize(150, 400);
			dialog.open();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ProjectWizard.class);
			log.error("failed to create project", e);
		}
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
			super("NetComponentWizardPage", "#Neue Komponente", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			createComboGroup(root);
			createList(root);
			createAmountRow(root);
		}

		private void createList(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridData(composite, true, true);
			UI.gridLayout(composite, 1);
			List list = new List(composite, SWT.BORDER);
			UI.gridData(list, true, true);
		}

		private void createComboGroup(Composite root) {
			Composite composite = UI.formComposite(root);
			UI.gridData(composite, true, false);
			UI.formCombo(composite, "#Komponente");
			UI.formCombo(composite, "#Größenklasse");
		}

		private void createAmountRow(Composite root) {
			Composite composite = new Composite(root, SWT.NONE);
			UI.gridData(composite, true, false);
			UI.gridLayout(composite, 3);
			UI.formText(composite, "#Menge");
			UI.formLabel(composite, "#Einheit");
		}
	}
}
