package sophena.rcp.editors.producers;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;

import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ProducerEditor extends Editor {

	private String projectId;
	private Producer producer;

	public static void open(ProjectDescriptor project,
			ProducerDescriptor producer) {
		if (project == null || producer == null)
			return;
		EditorInput input = new EditorInput(producer.id,
				producer.getName());
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
		setPartName(producer.getName());
	}

	private Producer findProducer(Project project, String producerKey) {
		if (project == null)
			return null;
		for (Producer p : project.getProducers()) {
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
			log.info("update producer {} in project {}", producer, projectId);
			ProjectDao dao = new ProjectDao(App.getDb());
			Project project = dao.get(projectId);
			Producer old = findProducer(project, producer.id);
			project.getProducers().remove(old);
			project.getProducers().add(producer);
			project = dao.update(project);
			producer = findProducer(project, producer.id);
			setPartName(producer.getName());
			Navigator.refresh();
			setSaved();
		} catch (Exception e) {
			log.error("failed to update project " + projectId, e);
		}
	}

	private static class EditorInput extends KeyEditorInput {

		private String projectKey;

		private EditorInput(String consumerKey, String name) {
			super(consumerKey, name);
		}
	}

}
