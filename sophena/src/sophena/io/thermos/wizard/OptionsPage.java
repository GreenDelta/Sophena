package sophena.io.thermos.wizard;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.UI;

public class OptionsPage extends WizardPage {

	private final ImportConfig config;

	private Button consumersCheck;
	private Button transferStationsCheck;
	private Button pipesCheck;
	private Button updateRadio;
	private Button addRadio;
	private Text fileText;

	public OptionsPage(ImportConfig config) {
		super("OptionsPage", "Import aus BioHeating-Tool", null);
		this.config = config;
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

		initData();
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
		updateRadio.addListener(SWT.Selection, e -> onSelectionChanged());

		addRadio = new Button(group, SWT.RADIO);
		addRadio.setText("ergänzen");
		addRadio.addListener(SWT.Selection, e -> onSelectionChanged());
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
			config.setFile(file);
			fileText.setText(file.getAbsolutePath());
			onSelectionChanged();
		}
	}

	private void initData() {
		if (config.getFile() != null) {
			fileText.setText(config.getFile().getAbsolutePath());
		}
		consumersCheck.setSelection(config.isImportConsumers());
		transferStationsCheck.setSelection(config.isImportTransferStations());
		pipesCheck.setSelection(config.isImportPipes());
		updateRadio.setSelection(config.isUpdateExisting());
		addRadio.setSelection(!config.isUpdateExisting());
	}

	private void onSelectionChanged() {
		updateConfig();
		validate();
		getContainer().updateButtons();
	}

	private void updateConfig() {
		config.setImportConsumers(consumersCheck.getSelection());
		config.setImportTransferStations(transferStationsCheck.getSelection());
		config.setImportPipes(pipesCheck.getSelection());
		config.setUpdateExisting(updateRadio.getSelection());
	}

	private void validate() {
		if (config.getFile() == null) {
			setErrorMessage("Bitte wählen Sie eine Importdatei.");
			setPageComplete(false);
			return;
		}
		if (!config.isImportConsumers()
				&& !config.isImportTransferStations()
				&& !config.isImportPipes()) {
			setErrorMessage("Bitte wählen Sie mindestens eine Importoption.");
			setPageComplete(false);
			return;
		}
		setErrorMessage(null);
		setPageComplete(true);
	}

	@Override
	public boolean canFlipToNextPage() {
		if (config.getFile() == null)
			return false;
		// Can flip to next page only if transfer stations or pipes is selected
		return config.isImportTransferStations() || config.isImportPipes();
	}
}
