package sophena.rcp.editors.consumers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ConsumerLoadCurve;
import sophena.db.daos.ProjectDao;
import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Location;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.model.descriptors.ConsumerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.LocationPage;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;

public class ConsumerEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());
	Consumer consumer;
	private String projectId;
	private WeatherStation weatherStation;

	private List<LoadProfileListener> profileListeners = new ArrayList<>();

	public static void open(ProjectDescriptor p, ConsumerDescriptor c) {
		if (p == null || c == null)
			return;
		EditorInput input = new EditorInput(c.id, c.name);
		input.projectKey = p.id;
		Editors.open(input, "sophena.ConsumerEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		Project project = dao.get(i.projectKey);
		projectId = project.id;
		consumer = findConsumer(project, i.getKey());
		this.weatherStation = project.weatherStation;
		setPartName(consumer.name);
		if (consumer.location == null) {
			Location loc = new Location();
			loc.id = UUID.randomUUID().toString();
			consumer.location = loc;
		}
	}

	private Consumer findConsumer(Project project, String consumerKey) {
		if (project == null)
			return null;
		for (Consumer c : project.consumers) {
			if (Objects.equals(consumerKey, c.id))
				return c;
		}
		log.error("did not found consumer {} in {}", consumerKey, project);
		return null;
	}

	public void calculate() {
		LoadProfile profile = ConsumerLoadCurve.calculate(consumer,
				weatherStation);
		double[] totals = profile.calculateTotal();
		double total = Stats.sum(totals);
		for (LoadProfileListener fn : profileListeners) {
			fn.update(profile, totals, total);
		}
	}

	public void onCalculated(LoadProfileListener fn) {
		profileListeners.add(fn);
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
			addPage(new LocationPage(this,
					() -> consumer.location,
					() -> new ProjectDao(App.getDb()).get(projectId)));
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
			Consumer old = findConsumer(project, consumer.id);
			project.consumers.remove(old);
			project.consumers.add(consumer);
			project = dao.update(project);
			consumer = findConsumer(project, consumer.id);
			setPartName(consumer.name);
			Navigator.refresh();
			setSaved();
		} catch (Exception e) {
			log.error("failed to update project " + projectId, e);
		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override
	public void doSaveAs() {
		InputDialog dialog = new InputDialog(UI.shell(), "Speichern unter",
				"Name des Abnehmers", consumer.name + " - Kopie", null);
		if (dialog.open() != Window.OK)
			return;
		Consumer clone = consumer.clone();
		clone.name = dialog.getValue();
		ProjectDao dao = new ProjectDao(App.getDb());
		Project project = dao.get(projectId);
		project.consumers.add(clone);
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
