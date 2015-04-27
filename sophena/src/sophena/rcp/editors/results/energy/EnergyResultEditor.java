package sophena.rcp.editors.results.energy;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ProjectCalculator;
import sophena.calc.ProjectResult;
import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.utils.Cache;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Rcp;

public class EnergyResultEditor extends FormEditor {

	private Project project;
	private ProjectResult result;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		Rcp.run("Berechne...", () -> {
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(d.getId());
			ProjectResult result = ProjectCalculator.calculate(p);
			Object[] data = new Object[] { p, result };
			String key = Cache.put(data);
			KeyEditorInput input = new KeyEditorInput(key, p.getName());
			Editors.open(input, "sophena.EnergyResultEditor");
		});
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			KeyEditorInput kei = (KeyEditorInput) input;
			Object[] data = Cache.remove(kei.getKey());
			project = (Project) data[0];
			result = (ProjectResult) data[1];
			setPartName(project.getName());
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to init energy result editor", e);
		}
	}

	public ProjectResult getResult() {
		return result;
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EnergyResultPage(this));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to init energy result editor", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

}
