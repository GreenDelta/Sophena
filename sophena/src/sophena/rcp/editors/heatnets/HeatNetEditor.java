package sophena.rcp.editors.heatnets;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;

public class HeatNetEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());

	protected Project project;
	protected HeatNet heatNet;

	public static void open(ProjectDescriptor project) {
		if (project == null)
			return;
		EditorInput input = new EditorInput(project.id + "/net", project.name);
		input.projectId = project.id;
		Editors.open(input, "sophena.HeatNetEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		project = dao.get(i.projectId);
		heatNet = project.heatNet;
		setPartName(project.name + " - Wärmenetz");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new HeatNetPage(this));
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		if (!valid())
			return;
		try {
			log.info("update heat net in project {}", project);
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(project.id);
			p.heatNet = heatNet;
			project = dao.update(p);
			heatNet = project.heatNet;
			setSaved();
		} catch (Exception e) {
			log.error("failed to update heat net in project " + project, e);
		}
	}

	private boolean valid() {
		if (heatNet.returnTemperature >= heatNet.supplyTemperature) {
			MsgBox.error("Plausibilitätsfehler",
					"Die Rücklauftemperatur ist größer oder gleich der "
							+ "Vorlauftemperatur.");
			return false;
		}
		if (heatNet.returnTemperature >= heatNet.maxBufferLoadTemperature) {
			MsgBox.error("Plausibilitätsfehler",
					"Die Rücklauftemperatur ist größer oder gleich der maximalen "
							+ "Ladetemperatur des Pufferspeichers.");
			return false;
		}
		Double lowerBuffTemp = heatNet.lowerBufferLoadTemperature;
		if (lowerBuffTemp != null
				&& lowerBuffTemp > heatNet.maxBufferLoadTemperature) {
			MsgBox.error("Plausibilitätsfehler",
					"Die untere Ladetemperatur ist größer als die maximale "
							+ "Ladetemperatur des Pufferspeichers.");
			return false;
		}
		if (heatNet.simultaneityFactor < 0 || heatNet.simultaneityFactor > 1) {
			MsgBox.error("Plausibilitätsfehler",
					"Der Gleichzeitigkeitsfaktor muss zwischen 0 und 1 liegen.");
			return false;
		}
		if (heatNet.smoothingFactor < 0) {
			MsgBox.error("Plausibilitätsfehler",
					"Der Glättungsfaktor darf nicht negativ sein.");
			return false;
		}
		return true;
	}

	private static class EditorInput extends KeyEditorInput {

		private String projectId;

		public EditorInput(String key, String name) {
			super(key, name);
		}

	}
}
