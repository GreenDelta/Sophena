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
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class ProducerProfileWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Project project;
	private Producer producer;
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
		ProducerProfileWizard wiz = new ProducerProfileWizard();
		wiz.setWindowTitle("Erzeugerlastgang integrieren");
		wiz.project = project;
		wiz.producer = initProducer(project);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		if (dialog.open() == Window.OK)
			Navigator.refresh();
	}

	private static Producer initProducer(Project project) {
		Producer p = new Producer();
		int i = 1;
		for (Producer other : project.producers) {
			if (other.hasProfile)
				i++;
		}
		p.id = UUID.randomUUID().toString();
		p.name = "Erzeugerlastgang " + i;
		p.hasProfile = true;
		p.costs = new ProductCosts();
		return p;
	}

	@Override
	public boolean performFinish() {
		try {
			project.producers.add(producer);
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.update(project);
			Navigator.refresh();
			ProducerEditor.open(
					project.toDescriptor(),
					producer.toDescriptor());
			return true;
		} catch (Exception e) {
			log.error("failed to update project with new producer", e);
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
			super("ProducerProfilePage", "Erzeugerlastgang integrieren", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			Composite comp = UI.formComposite(root);
			UI.gridData(comp, true, false);
			name(comp);
			// groupCombo(comp);
			// boilerTable(root);
			// functionFields(root);
		}

		private void name(Composite comp) {
			Text t = UI.formText(comp, M.Name);
			Texts.on(t).init(producer.name).required().onChanged(s -> {
				producer.name = s;
			});
		}
	}

}
