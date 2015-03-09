package sophena.rcp.editors.consumers;

import java.util.Objects;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.daos.ProjectDao;
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Cache;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ConsumerEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Project project;
	private Consumer consumer;
	private boolean dirty;

	public static void open(Project project, Consumer consumer) {
		if (consumer == null)
			return;
		String key = Cache.put(project);
		EditorInput input = new EditorInput(key, consumer.getName());
		input.consumerKey = consumer.getId();
		Editors.open(input, "sophena.ConsumerEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		project = Cache.remove(i.getKey());
		consumer = findConsumer(project, i.consumerKey);
		setPartName(consumer.getName());
	}

	private Consumer findConsumer(Project project, String consumerKey) {
		for (Consumer c : project.getConsumers()) {
			if (Objects.equals(consumerKey, c.getId()))
				return c;
		}
		log.error("did not found consumer {} in {}", consumerKey, project);
		return null;
	}

	public Consumer getConsumer() {
		return consumer;
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
			log.info("update consumer {} in project {}", consumer, project);
			ProjectDao dao = new ProjectDao(App.getDb());
			project = dao.update(project);
			consumer = findConsumer(project, consumer.getId());
			dirty = false;
			setPartName(consumer.getName());
			Navigator.refresh(consumer);
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

		private String consumerKey;

		private EditorInput(String projectKey, String name) {
			super(projectKey, name);
		}
	}
}
