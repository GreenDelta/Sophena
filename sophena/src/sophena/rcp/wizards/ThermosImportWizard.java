package sophena.rcp.wizards;

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.UI;

public class ThermosImportWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final ProjectDescriptor project;

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
		setNeedsProgressMonitor(false);
	}

	@Override
	public void addPages() {
		optionsPage = new OptionsPage();
		transferStationsPage = new TransferStationsPage();
		pipesPage = new PipesPage();
		addPage(optionsPage);
		addPage(transferStationsPage);
		addPage(pipesPage);
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page == optionsPage) {
			if (optionsPage.transferStationsCheck.getSelection()) {
				return transferStationsPage;
			}
			if (optionsPage.pipesCheck.getSelection()) {
				return pipesPage;
			}
			return null;
		}
		if (page == transferStationsPage) {
			if (optionsPage.pipesCheck.getSelection()) {
				return pipesPage;
			}
			return null;
		}
		return null;
	}

	@Override
	public boolean canFinish() {
		// Can finish if file is selected and at least one option is checked
		if (optionsPage.selectedFile == null)
			return false;
		if (!optionsPage.consumersCheck.getSelection()
				&& !optionsPage.transferStationsCheck.getSelection()
				&& !optionsPage.pipesCheck.getSelection()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean performFinish() {
		try {
			log.info("ThermosImportWizard: performFinish called");
			log.info("  Project: {}", project.name);
			log.info("  File: {}", optionsPage.selectedFile);
			log.info("  Consumers: {}", optionsPage.consumersCheck.getSelection());
			log.info("  Transfer Stations: {}", optionsPage.transferStationsCheck.getSelection());
			log.info("  Pipes: {}", optionsPage.pipesCheck.getSelection());
			log.info("  Mode: {}", optionsPage.updateRadio.getSelection() ? "update" : "add");
			// TODO: Actual import logic to be implemented later
			return true;
		} catch (Exception e) {
			log.error("Failed to import from BioHeating-Tool", e);
			return false;
		}
	}

	private class OptionsPage extends WizardPage {

		Button consumersCheck;
		Button transferStationsCheck;
		Button pipesCheck;
		Button updateRadio;
		Button addRadio;
		Text fileText;
		File selectedFile;

		OptionsPage() {
			super("OptionsPage", "Import aus BioHeating-Tool", null);
			setMessage("Wählen Sie die zu importierenden Daten und die Importdatei.");
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 10, 10);

			createImportOptionsGroup(root);
			createDataModeGroup(root);
			createFileGroup(root);

			validate();
		}

		private void createImportOptionsGroup(Composite parent) {
			Group group = new Group(parent, SWT.NONE);
			group.setText("Was möchten Sie hinzufügen?");
			UI.gridData(group, true, false);
			UI.gridLayout(group, 1, 5, 10);

			consumersCheck = new Button(group, SWT.CHECK);
			consumersCheck.setText("Abnehmer");
			consumersCheck.addListener(SWT.Selection, e -> onSelectionChanged());

			transferStationsCheck = new Button(group, SWT.CHECK);
			transferStationsCheck.setText("Hausübergabestationen");
			transferStationsCheck.addListener(SWT.Selection, e -> onSelectionChanged());

			pipesCheck = new Button(group, SWT.CHECK);
			pipesCheck.setText("Wärmeleitungen");
			pipesCheck.addListener(SWT.Selection, e -> onSelectionChanged());
		}

		private void createDataModeGroup(Composite parent) {
			Group group = new Group(parent, SWT.NONE);
			group.setText("Bereits vorhandene Daten");
			UI.gridData(group, true, false);
			UI.gridLayout(group, 1, 5, 10);

			updateRadio = new Button(group, SWT.RADIO);
			updateRadio.setText("aktualisieren");
			updateRadio.setSelection(true);

			addRadio = new Button(group, SWT.RADIO);
			addRadio.setText("ergänzen");
		}

		private void createFileGroup(Composite parent) {
			Group group = new Group(parent, SWT.NONE);
			group.setText("Importdatei");
			UI.gridData(group, true, false);
			UI.gridLayout(group, 2, 5, 10);

			fileText = new Text(group, SWT.BORDER | SWT.READ_ONLY);
			UI.gridData(fileText, true, false);

			Button browseBtn = new Button(group, SWT.PUSH);
			browseBtn.setText("Durchsuchen...");
			browseBtn.addListener(SWT.Selection, e -> onBrowse());
		}

		private void onBrowse() {
			File file = FileChooser.open("*.json", "*.xlsx", "*.*");
			if (file != null) {
				selectedFile = file;
				fileText.setText(file.getAbsolutePath());
				onSelectionChanged();
			}
		}

		private void onSelectionChanged() {
			validate();
			getContainer().updateButtons();
		}

		private void validate() {
			if (selectedFile == null) {
				setErrorMessage("Bitte wählen Sie eine Importdatei.");
				setPageComplete(false);
				return;
			}
			if (!consumersCheck.getSelection()
					&& !transferStationsCheck.getSelection()
					&& !pipesCheck.getSelection()) {
				setErrorMessage("Bitte wählen Sie mindestens eine Importoption.");
				setPageComplete(false);
				return;
			}
			setErrorMessage(null);
			setPageComplete(true);
		}

		@Override
		public boolean canFlipToNextPage() {
			if (selectedFile == null)
				return false;
			// Can flip to next page only if transfer stations or pipes is selected
			return transferStationsCheck.getSelection() || pipesCheck.getSelection();
		}

		@Override
		public IWizardPage getNextPage() {
			return ThermosImportWizard.this.getNextPage(this);
		}
	}

	private class TransferStationsPage extends WizardPage {

		TransferStationsPage() {
			super("TransferStationsPage", "Hausübergabestationen", null);
			setMessage("Konfiguration für den Import der Hausübergabestationen.");
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 10, 10);

			var label = new org.eclipse.swt.widgets.Label(root, SWT.NONE);
			label.setText("Konfiguration Hausübergabestationen\n\n"
					+ "Hier können später weitere Optionen für den Import\n"
					+ "der Hausübergabestationen konfiguriert werden.");
			UI.gridData(label, true, true);
		}

		@Override
		public IWizardPage getNextPage() {
			return ThermosImportWizard.this.getNextPage(this);
		}
	}

	private class PipesPage extends WizardPage {

		PipesPage() {
			super("PipesPage", "Wärmeleitungen", null);
			setMessage("Konfiguration für den Import der Wärmeleitungen.");
		}

		@Override
		public void createControl(Composite parent) {
			Composite root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 10, 10);

			var label = new org.eclipse.swt.widgets.Label(root, SWT.NONE);
			label.setText("Konfiguration Wärmeleitungen\n\n"
					+ "Hier können später weitere Optionen für den Import\n"
					+ "der Wärmeleitungen konfiguriert werden.");
			UI.gridData(label, true, true);
		}

		@Override
		public IWizardPage getNextPage() {
			return null;
		}
	}
}
