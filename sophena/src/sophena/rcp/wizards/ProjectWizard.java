package sophena.rcp.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

import sophena.rcp.utils.UI;

public class ProjectWizard extends Wizard implements INewWizard {

	private static final String ID = "sophena.ProjectWizard";

	private Page page;

	public static void open() {
		try {
			ProjectWizard wizard = (ProjectWizard) PlatformUI.getWorkbench()
					.getNewWizardRegistry().findWizard(ID).createWizard();
			WizardDialog dialog = new WizardDialog(UI.shell(), wizard);
			dialog.setTitle("#Neues Projekt erstellen");
			dialog.setPageSize(150, 350);
			dialog.open();
			UI.center(UI.shell(), dialog.getShell());
			// TODO: Navigator.refresh(parent);
		} catch (Exception e) {
			e.printStackTrace(); // TODO: log
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("#Neues Projekt erstellen");
		// TODO: set project image
	}

	@Override
	public boolean performFinish() {
		// TODO create Project
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Text nameText;
		private Text descriptionText;
		private Text timeText;

		protected Page() {
			super("ProjectWizardPage", "#Neues Projekt erstellen", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			setControl(composite);
			nameText = UI.formText(composite, "#Name");
			nameText.setText("#Neues Projekt");
			descriptionText = UI.formMultiText(composite, "#Beschreibung");
			timeText = UI.formText(composite, "#Projektlaufzeit (Jahre)");
			timeText.setText("5");
		}

	}

}
