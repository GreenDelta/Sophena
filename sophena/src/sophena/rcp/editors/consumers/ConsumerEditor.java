package sophena.rcp.editors.consumers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ConsumerLoadCurve;
import sophena.db.daos.ProjectDao;
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.WeatherStation;
import sophena.model.descriptors.ConsumerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ConsumerEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Consumer consumer;
	private String projectId;
	private WeatherStation weatherStation; // only used for calculations
	private boolean dirty;

	private List<java.util.function.Consumer<double[]>> calcListeners = new ArrayList<>();

	public static void open(ProjectDescriptor p, ConsumerDescriptor c) {
		if (p == null || c == null)
			return;
		EditorInput input = new EditorInput(c.getId(), c.getName());
		input.projectKey = p.getId();
		Editors.open(input, "sophena.ConsumerEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		Project project = dao.get(i.projectKey);
		projectId = project.getId();
		consumer = findConsumer(project, i.getKey());
		this.weatherStation = project.getWeatherStation();
		setPartName(consumer.getName());
	}

	private Consumer findConsumer(Project project, String consumerKey) {
		if (project == null)
			return null;
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

	public void calculate() {
		double[] loadCurve = ConsumerLoadCurve.calculate(consumer,
				weatherStation);
		for (java.util.function.Consumer<double[]> fn : calcListeners) {
			fn.accept(loadCurve);
		}
	}

	public void onCalculated(java.util.function.Consumer<double[]> fn) {
		calcListeners.add(fn);
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
			addPage(new LocationPage(this));
			addPage(new LoadProfilesPage(this));
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			log.info("update consumer {} in project {}", consumer, projectId);
			ProjectDao dao = new ProjectDao(App.getDb());
			Project project = dao.get(projectId);
			Consumer old = findConsumer(project, consumer.getId());
			project.getConsumers().remove(old);
			project.getConsumers().add(consumer);
			project = dao.update(project);
			consumer = findConsumer(project, consumer.getId());
			setPartName(consumer.getName());
			Navigator.refresh();
			dirty = false;
			editorDirtyStateChanged();
		} catch (Exception e) {
			log.error("failed to update project " + projectId, e);
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
