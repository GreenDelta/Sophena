package sophena.rcp.editors.results.single;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ProjectResult;
import sophena.db.daos.ProjectDao;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Rcp;
import sophena.rcp.utils.Strings;

public class ResultEditor extends Editor {

	Project project;
	ProjectResult result;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		closeExisting(d);
		Rcp.run("Berechne...", () -> {
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(d.id);
			ProjectResult result = ProjectResult.calculate(p);
			Object[] data = new Object[] { p, result };
			String key = App.stash(data);
			KeyEditorInput input = new KeyEditorInput(key, p.name);
			Editors.open(input, "sophena.ResultEditor");
		});
	}

	private static void closeExisting(ProjectDescriptor d) {
		for (IEditorReference ref : Editors.getReferences()) {
			IEditorPart e = ref.getEditor(false);
			if (!(e instanceof ResultEditor))
				continue;
			ResultEditor editor = (ResultEditor) e;
			if (Strings.nullOrEqual(d.id, editor.project.id))
				Editors.close(ref);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		try {
			KeyEditorInput kei = (KeyEditorInput) input;
			Object[] data = App.pop(kei.getKey());
			project = (Project) data[0];
			result = (ProjectResult) data[1];
			setPartName(project.name);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to init energy result editor", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EnergyResultPage(this));
			if (isWithCoGen())
				addPage(new ElectricityResultPage(this));
			addPage(new CostResultPage(this));
			addPage(new LocationResultPage(this));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to init energy result editor", e);
		}
	}

	private boolean isWithCoGen() {
		if (result == null || result.energyResult == null)
			return false;
		for (Producer p : result.energyResult.producers) {
			if (p.boiler != null && p.boiler.isCoGenPlant)
				return true;
		}
		return false;
	}

}
