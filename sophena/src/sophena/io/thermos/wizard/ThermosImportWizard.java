package sophena.io.thermos.wizard;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.io.thermos.ThermosImport;
import sophena.io.thermos.ThermosImportConfig;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.UI;

public class ThermosImportWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ThermosImportConfig config;

	private OptionsPage optionsPage;
	private TransferStationsPage transferStationsPage;
	private PipesPage pipesPage;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		var project = App.getDb().get(Project.class, d.id);
		if (project == null)
			return;

		var wiz = new ThermosImportWizard(project);
		wiz.setWindowTitle("Import aus BioHeating-Tool");
		var dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(500, 400);
		if (dialog.open() == Window.OK) {
			Navigator.refresh();
		}
	}

	private ThermosImportWizard(Project project) {
		this.config = new ThermosImportConfig(project);
		setNeedsProgressMonitor(false);
	}

	@Override
	public void addPages() {
		optionsPage = new OptionsPage(config);
		transferStationsPage = new TransferStationsPage();
		pipesPage = new PipesPage();
		addPage(optionsPage);
		addPage(transferStationsPage);
		addPage(pipesPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == optionsPage && config.isWithStations())
			return transferStationsPage;
		return page != pipesPage && config.isWithPipes()
			? pipesPage
			: null;
	}

	@Override
	public boolean canFinish() {
		return config.canRunImport();
	}

	@Override
	public boolean performFinish() {
		if (!config.canRunImport())
			return false;
		try {
			getContainer().run(true, false, monitor -> {
				monitor.beginTask("Importiere Daten ...", IProgressMonitor.UNKNOWN);
				new ThermosImport(App.getDb(), config).run();
				monitor.done();
			});
			return true;
		} catch (Exception e) {
			MsgBox.error("Ein unerwarteter Fehler ist aufgetreten: " + e.getMessage());
			log.error("Import failed", e);
			return false;
		} finally {
			Navigator.refresh();
		}
	}
}
