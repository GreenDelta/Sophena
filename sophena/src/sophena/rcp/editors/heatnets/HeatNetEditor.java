package sophena.rcp.editors.heatnets;

import java.time.MonthDay;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.TimeInterval;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.MsgBox;

public class HeatNetEditor extends Editor {

	private final Logger log = LoggerFactory.getLogger(getClass());

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
		var i = (EditorInput) input;
		var dao = new ProjectDao(App.getDb());
		project = dao.get(i.projectId);
		if (project.heatNet == null) {
			HeatNet.addDefaultTo(project);
		}
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
			var dao = new ProjectDao(App.getDb());
			var p = dao.get(project.id);
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

		/*
		Double lowerBuffTemp = heatNet.lowerBufferLoadTemperature;
		if (lowerBuffTemp != null
				&& lowerBuffTemp > heatNet.maxBufferLoadTemperature) {
			MsgBox.error("Plausibilitätsfehler",
					"Die untere Ladetemperatur ist größer als die maximale "
							+ "Ladetemperatur des Pufferspeichers.");
			return false;
		}
		*/
		
		if (heatNet.simultaneityFactor < 0 || heatNet.simultaneityFactor > 1) {
			MsgBox.error("Plausibilitätsfehler",
					"Der Gleichzeitigkeitsfaktor muss zwischen 0 und 1 liegen.");
			return false;
		}
		if (heatNet.smoothingFactor != null && heatNet.smoothingFactor < 0) {
			MsgBox.error("Plausibilitätsfehler",
					"Der Glättungsfaktor darf nicht negativ sein.");
			return false;
		}
		if (heatNet.bufferTank != null && heatNet.maximumPerformance <= 0)
		{
			MsgBox.error("Plausibilitätsfehler",
					"Die maximale Entladeleistung muss größer als 0 sein.");
			return false;
		}
		if(heatNet.isSeasonalDrivingStyle)
		{
			TimeInterval intervalSummer = heatNet.intervalSummer;
			TimeInterval intervalWinter = heatNet.intervalWinter;
			if((intervalSummer.start != null) && (intervalWinter.end != null) && MonthDay.parse(intervalSummer.start).compareTo(MonthDay.parse(intervalWinter.end)) > 0)
			{
				MsgBox.error("Plausibilitätsfehler",
						M.StartSummerError);
				return false;
			}	
			if((intervalSummer.end != null) && (intervalWinter.start != null) && MonthDay.parse(intervalSummer.end).compareTo(MonthDay.parse(intervalWinter.start)) > 0)
			{
				MsgBox.error("Plausibilitätsfehler",
						M.EndSummerError);
				return false;
			}	
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
