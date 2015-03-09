package sophena.rcp.wizards;

import java.util.UUID;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

public class ProjectWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Page page;

	public static void open() {
		try {
			ProjectWizard wizard = new ProjectWizard();
			wizard.setWindowTitle(M.CreateNewProject);
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
			super("ProjectWizardPage", M.CreateNewProject, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			setControl(composite);
			nameText = UI.formText(composite, M.Name);
			nameText.setBackground(Colors.forRequiredField());
			nameText.setText(M.NewProject);
			descriptionText = UI.formMultiText(composite, M.Description);
			timeText = UI.formText(composite, M.ProjectDurationYears);
			timeText.setText("20");
			timeText.setBackground(Colors
					.getSystemColor(SWT.COLOR_INFO_BACKGROUND));
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
