package sophena.rcp.editors.results.single;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
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
import sophena.utils.Strings;

public class ResultEditor extends Editor {

	Project project;
	ProjectResult result;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		PlatformUI.getWorkbench().saveAllEditors(true);
		Editors.closeIf(editor -> {
			if (!(editor instanceof ResultEditor))
				return false;
			ResultEditor e = (ResultEditor) editor;
			return Strings.nullOrEqual(d.id, e.project.id);
		});
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

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		onClosed(() -> {
			// cache the last opened page when the editor closes
			int page = getActivePage();
			App.stash(":last-result-page", project.id + "_@" + page);
		});
		try {
			KeyEditorInput kei = (KeyEditorInput) input;
			Object[] data = App.pop(kei.getKey());
			project = (Project) data[0];
			result = (ProjectResult) data[1];
			setPartName(project.name + " - Ergebnisse");
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
			addPage(new FurtherResultsPage(this));
			addPage(new ConsumerResultPage(this));
			addPage(new LocationResultPage(this));
			addPage(new LogPage(this));
			activateLastPage();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to init energy result editor", e);
		}
	}

	/**
	 * Activate the last opened page if the results of the project were already
	 * shown.
	 */
	private void activateLastPage() {
		String pageDef = App.pop(":last-result-page");
		if (pageDef == null)
			return;
		String[] parts = pageDef.split("_@");
		if (parts.length < 2)
			return;
		if (!Strings.nullOrEqual(parts[0], project.id))
			return;
		int page = Integer.parseInt(parts[1]);
		if (pages != null && pages.size() > page)
			setActivePage(page);
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

	@Override
	public void close(boolean save) {
		System.out.println(getActivePage());
		super.close(save);
	}

}
