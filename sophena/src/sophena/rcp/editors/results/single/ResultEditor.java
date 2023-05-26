package sophena.rcp.editors.results.single;

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ProjectResult;
import sophena.db.daos.ProjectDao;
import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.colors.ResultColors;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.results.CalculationCheck;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Rcp;
import sophena.utils.Strings;

public class ResultEditor extends Editor {

	Project project;
	ProjectResult result;
	ResultColors colors;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		PlatformUI.getWorkbench().saveAllEditors(true);
		Editors.closeIf(editor -> {
			if (!(editor instanceof ResultEditor e))
				return false;
			return Strings.nullOrEqual(d.id, e.project.id);
		});
		Project p = new ProjectDao(App.getDb()).get(d.id);
		if (!CalculationCheck.canCalculate(p))
			return;
		Rcp.run("Berechne...", () -> {
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
			colors = ResultColors.of(result);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to init energy result editor", e);
		}
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EnergyResultPage(this));
			if (isWithCoGen()) {
				addPage(new ElectricityResultPage(this));
			}
			addPage(new CostResultPage(this));
			addPage(new FurtherResultsPage(this));
			addPage(new ConsumerResultPage(this));
			addPage(new LocationResultPage(this));
			// addPage(new LogPage(this));
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
		if (pages == null)
			return;
		String pageDef = App.pop(":last-result-page");
		if (pageDef == null)
			return;
		String[] parts = pageDef.split("_@");
		if (parts.length < 2)
			return;
		if (!Strings.nullOrEqual(parts[0], project.id))
			return;
		try {
			int page = Integer.parseInt(parts[1]);
			if (page < 0 || page >= pages.size())
				return; // the page is -1 if there was no active page
			setActivePage(page);
		} catch (Exception e) {
			// this is a very optional feature; so if
			// something went wrong, we just ignore it
		}
	}

	private boolean isWithCoGen() {
		if (result == null || result.energyResult == null)
			return false;
		for (Producer p : result.energyResult.producers) {
			if (Producers.electricPower(p) > 0)
				return true;
		}
		return false;
	}

}
