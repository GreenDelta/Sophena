package sophena.rcp.editors.producers;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.db.daos.ProjectDao;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.UI;

public class ProducerEditor extends Editor {

	private String projectId;
	private Producer producer;

	public static void open(ProjectDescriptor project,
			ProducerDescriptor producer) {
		if (project == null || producer == null)
			return;
		EditorInput input = new EditorInput(producer.id,
				producer.name);
		input.projectKey = project.id;
		Editors.open(input, "sophena.ProducerEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		Project project = dao.get(i.projectKey);
		projectId = project.id;
		producer = findProducer(project, i.getKey());
		if (producer == null)
			return;
		setPartName(producer.name);
	}

	private Producer findProducer(Project project, String producerKey) {
		if (project == null)
			return null;
		for (Producer p : project.producers) {
			if (Objects.equals(producerKey, p.id))
				return p;
		}
		return null;
	}

	public Producer getProducer() {
		return producer;
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			ProjectDao dao = new ProjectDao(App.getDb());
			Project project = dao.get(projectId);
			if (!valid(project))
				return;
			log.info("update producer {} in project {}", producer, projectId);
			Producer old = findProducer(project, producer.id);
			project.producers.remove(old);
			project.producers.add(producer);
			project = dao.update(project);
			producer = findProducer(project, producer.id);
			setPartName(producer.name);
			Navigator.refresh();
			setSaved();
		} catch (Exception e) {
			log.error("failed to update project " + projectId, e);
		}
	}

	private boolean valid(Project project) {
		FuelSpec fuelSpec = producer.fuelSpec;
		if (fuelSpec != null && fuelSpec.woodAmountType != null) {
			if (fuelSpec.waterContent < 0 || fuelSpec.waterContent > 60) {
				MsgBox.error("Plausibilitätsfehler",
						"Der Wassergehalt muss zwischen 0% und 60% liegen.");
				return false;
			}
		}
		for (Producer other : project.producers) {
			if (Objects.equals(other, producer))
				continue;
			if (other.rank != producer.rank)
				continue;
			MsgBox.error("Plausibilitätsfehler",
					"Der Rang des Erzeugers ist bereits vergeben.");
			return false;
		}
		return true;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSaveAs() {
		InputDialog dialog = new InputDialog(UI.shell(), "Speichern unter",
				"Name des Erzeugers", producer.name + " - Kopie", null);
		if (dialog.open() != Window.OK)
			return;
		Producer clone = producer.clone();
		clone.name = dialog.getValue();
		ProjectDao dao = new ProjectDao(App.getDb());
		Project project = dao.get(projectId);
		project.producers.add(clone);
		dao.update(project);
		Navigator.refresh();
	}

	private static class EditorInput extends KeyEditorInput {

		private String projectKey;

		private EditorInput(String consumerKey, String name) {
			super(consumerKey, name);
		}
	}

}
