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
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

public class ConsumerProfileWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Project project;
	private Consumer consumer;
	private Page page;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		ProjectDao dao = new ProjectDao(App.getDb());
		open(dao.get(d.id));
	}

	public static void open(Project project) {
		if (project == null)
			return;
		ConsumerProfileWizard wiz = new ConsumerProfileWizard();
		wiz.setWindowTitle("Neuer Lastgang");
		wiz.project = project;
		wiz.consumer = initConsumer(project);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		if (dialog.open() == Window.OK) {
			Navigator.refresh();
		}
	}

	private static Consumer initConsumer(Project project) {
		Consumer c = new Consumer();
		int i = 1;
		for (Consumer other : project.consumers) {
			if (other.hasProfile())
				i++;
		}
		c.id = UUID.randomUUID().toString();
		c.name = "Lastgang " + i;
		return c;
	}

	@Override
	public boolean performFinish() {
		try {
			project.consumers.add(consumer);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.update(project);
			Navigator.refresh();
			ConsumerEditor.open(
					project.toDescriptor(),
					consumer.toDescriptor());
			return true;
		} catch (Exception e) {
			log.error("failed to update project with new consumer", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Page() {
			super("ConsumerProfilePage", "Neuer Lastgang", null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			Composite comp = UI.formComposite(root);
			UI.gridData(comp, true, false);
			name(comp);

		}

		private void name(Composite comp) {
			Text t = UI.formText(comp, M.Name);
			Texts.on(t).init(consumer.name).required().onChanged(s -> {
				consumer.name = s;
				validate();
			});
		}

		private boolean validate() {
			if (Strings.nullOrEmpty(consumer.name)) {
				return err("Der Name darf nicht leer sein.");
			}
			if (consumer.profile == null) {
				return err("Es wurde noch kein Lastgang ausgewählt");
			}
			setErrorMessage(null);
			setPageComplete(true);
			return true;
		}

		private boolean err(String msg) {
			setPageComplete(false);
			setErrorMessage(msg);
			return false;
		}

	}

}
