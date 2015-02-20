package sophena.rcp.wizards;

import java.util.UUID;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.UI;

public class ProjectWizard extends Wizard implements INewWizard {

	private static final String ID = "sophena.ProjectWizard";

	private Logger log = LoggerFactory.getLogger(getClass());

	private Page page;

	public static void open() {
		try {
			ProjectWizard wizard = new ProjectWizard();
			wizard.setWindowTitle("#Neues Projekt erstellen");
			WizardDialog dialog = new WizardDialog(UI.shell(), wizard);
			dialog.setPageSize(150, 350);
			if (dialog.open() == Window.OK)
				Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ProjectWizard.class);
			log.error("failed to create project", e);
		}
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle("#Neues Projekt erstellen");
		// TODO: set project image
	}

	@Override
	public boolean performFinish() {
		try {
			Project p = page.getProject();
			ProjectDao dao = new ProjectDao(App.getDb());
			ProjectEditor.open(p);
			dao.insert(p);
			return true;
		} catch (Exception e) {
			log.error("failed to create project", e);
			return false;
		}
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

		private Project getProject() {
			Project p = new Project();
			p.setId(UUID.randomUUID().toString());
			p.setName(nameText.getText());
			p.setDescription(descriptionText.getText());
			return p;
		}

	}

}
