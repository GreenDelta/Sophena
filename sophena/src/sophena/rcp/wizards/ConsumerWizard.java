package sophena.rcp.wizards;

import java.util.UUID;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Consumer;
import sophena.model.Project;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.UI;

public class ConsumerWizard extends Wizard implements INewWizard {

	private static final String ID = "sophena.ConsumerWizard";

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Project project;

	public static void open(Project project) {
		try {
			ConsumerWizard wizard = new ConsumerWizard();
			wizard.setWindowTitle("#Neuen Abnehmer erfassen");
			wizard.project = project;
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
		return false;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	private class Page extends WizardPage {

		private Text nameText;
		private Text descriptionText;
		private Combo typeCombo;
		private Combo stateCombo;

		private Page() {
			super("ConsumerWizardPage", "#Neuen Abnehmer erfassen", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = UI.formComposite(parent);
			setControl(composite);
			nameText = UI.formText(composite, "#Name");
			nameText.setText("#Neuer Abnehmer");
			descriptionText = UI.formMultiText(composite, "#Beschreibung");
			typeCombo = UI.formCombo(composite, "#Gebäudetype");
			stateCombo = UI.formCombo(composite, "#Gebäudezustand");
		}

		private Consumer getConsumer() {
			Consumer c = new Consumer();
			c.setId(UUID.randomUUID().toString());
			c.setName(nameText.getText());
			c.setDescription(descriptionText.getText());
			return c;
		}
	}

}
