package sophena.io.thermos.wizard;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.UI;

public class ThermosImportWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ProjectDescriptor project;
	private final ImportConfig config;

	private OptionsPage optionsPage;
	private TransferStationsPage transferStationsPage;
	private PipesPage pipesPage;

	public static void open(ProjectDescriptor project) {
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

	private ThermosImportWizard(ProjectDescriptor project) {
		this.project = project;
		this.config = new ImportConfig();
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
		if (page == optionsPage) {
			if (config.isImportTransferStations()) {
				return transferStationsPage;
			}
			if (config.isImportPipes()) {
				return pipesPage;
			}
			return null;
		}
		if (page == transferStationsPage) {
			if (config.isImportPipes()) {
				return pipesPage;
			}
			return null;
		}
		return null;
	}

	@Override
	public boolean canFinish() {
		// Can finish if file is selected and at least one option is checked
		if (config.getFile() == null)
			return false;
		if (!config.isImportConsumers()
				&& !config.isImportTransferStations()
				&& !config.isImportPipes()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean performFinish() {
		try {
			log.info("ThermosImportWizard: performFinish called");
			log.info("  Project: {}", project.name);
			log.info("  File: {}", config.getFile());
			log.info("  Consumers: {}", config.isImportConsumers());
			log.info("  Transfer Stations: {}", config.isImportTransferStations());
			log.info("  Pipes: {}", config.isImportPipes());
			log.info("  Mode: {}", config.isUpdateExisting() ? "update" : "add");
			// TODO: Actual import logic to be implemented later
			return true;
		} catch (Exception e) {
			log.error("Failed to import from BioHeating-Tool", e);
			return false;
		}
	}
}
