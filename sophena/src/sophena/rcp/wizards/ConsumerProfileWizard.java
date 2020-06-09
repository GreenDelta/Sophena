package sophena.rcp.wizards;

import java.io.File;
import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.io.ConsumerProfiles;
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

public class ConsumerProfileWizard extends Wizard {

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
		if (!page.isValid())
			return false;
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
			var log = LoggerFactory.getLogger(getClass());
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

		private Text fileText;

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
			Texts.on(UI.formText(comp, M.Name))
					.init(consumer.name).required()
					.onChanged(s -> {
						consumer.name = s;
						setErrorMessage(null);
					});
			Texts.on(UI.formMultiText(comp, M.Description))
					.onChanged(s -> consumer.description = s);
			fileFields(comp);
		}

		private void fileFields(Composite comp) {
			UI.formLabel(comp, "Lastgang");
			Composite fileComp = new Composite(comp, SWT.NONE);
			UI.gridData(fileComp, true, false);
			UI.innerGrid(fileComp, 2);
			fileText = new Text(fileComp, SWT.BORDER | SWT.READ_ONLY);
			fileText.setBackground(Colors.getWhite());
			UI.gridData(fileText, true, false);
			Button btn = new Button(fileComp, SWT.NONE);
			btn.setText("Öffnen");
			Controls.onSelect(btn, e -> onSelectFile());
		}

		private void onSelectFile() {
			setErrorMessage(null);
			File f = FileChooser.open("*.csv", "*.txt");
			if (f == null)
				return;

			var r = ConsumerProfiles.read(f, consumer);
			var message = r.message();

			// check error
			if (r.isError()) {
				consumer.profile = null;
				MsgBox.error(message.orElse(
						"Fehler beim Lesen der Datei"));
				return;
			}

			// display warnings
			if (r.isWarning() && message.isPresent()) {
				MsgBox.warn(message.get());
			}
			fileText.setText(f.getAbsolutePath());
		}

		private boolean isValid() {
			if (Strings.nullOrEmpty(consumer.name)) {
				return err("Der Name darf nicht leer sein.");
			}
			if (consumer.profile == null) {
				return err("Es wurde noch kein Lastgang ausgewählt");
			}
			setErrorMessage(null);
			return true;
		}

		private boolean err(String msg) {
			setErrorMessage(msg);
			return false;
		}
	}

}
