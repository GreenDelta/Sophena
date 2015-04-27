package sophena.rcp.editors.producers;

import java.util.Objects;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ProducerEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Project project;
	private Producer producer;
	private boolean dirty;

	public static void open(ProjectDescriptor project,
			ProducerDescriptor producer) {
		if (project == null || producer == null)
			return;
		EditorInput input = new EditorInput(producer.getId(),
				producer.getName());
		input.projectKey = project.getId();
		Editors.open(input, "sophena.ProducerEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		project = dao.get(i.projectKey);
		producer = findProducer(project, i.getKey());
		setPartName(producer.getName());
	}

	private Producer findProducer(Project project, String producerKey) {
		if (project == null)
			return null;
		for (Producer p : project.getProducers()) {
			if (Objects.equals(producerKey, p.getId()))
				return p;
		}
		return null;
	}

	public Producer getProducer() {
		return producer;
	}

	public Project getProject() {
		return project;
	}

	public void setDirty() {
		if (dirty)
			return;
		dirty = true;
		editorDirtyStateChanged();
	}

	@Override
	public boolean isDirty() {
		return dirty;
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
			log.info("update producer {} in project {}", producer, project);
			ProjectDao dao = new ProjectDao(App.getDb());
			project = dao.update(project);
			producer = findProducer(project, producer.getId());
			dirty = false;
			setPartName(producer.getName());
			Navigator.refresh();
			editorDirtyStateChanged();
		} catch (Exception e) {
			log.error("failed to update project " + project, e);
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	private static class EditorInput extends KeyEditorInput {

		private String projectKey;

		private EditorInput(String consumerKey, String name) {
			super(consumerKey, name);
		}
	}

}
