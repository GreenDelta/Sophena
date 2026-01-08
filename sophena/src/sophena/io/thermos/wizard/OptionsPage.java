package sophena.io.thermos.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import sophena.io.thermos.ThermosFile;
import sophena.io.thermos.ThermosImportConfig;
import sophena.rcp.App;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.UI;

class OptionsPage extends WizardPage {

	private final ThermosImportConfig config;

	private Button consumersCheck;
	private Button stationsCheck;
	private Button pipesCheck;
	private Button updateRadio;
	private Text fileText;

	OptionsPage(ThermosImportConfig config) {
		super("OptionsPage", "Import aus BioHeating-Tool", null);
		this.config = config;
		setMessage("Wählen Sie die zu importierenden Daten und die Importdatei.");
	}

	@Override
	public void createControl(Composite parent) {
		var root = new Composite(parent, SWT.NONE);
		setControl(root);
		UI.gridLayout(root, 1, 10, 10);
		createImportOptionsGroup(root);
		createDataModeGroup(root);
		createFileGroup(root);
		setPageComplete(false);
	}

	private void createImportOptionsGroup(Composite parent) {
		var group = new Group(parent, SWT.NONE);
		group.setText("Was möchten Sie hinzufügen?");
		UI.gridData(group, true, false);
		UI.gridLayout(group, 1, 5, 10);

		consumersCheck = new Button(group, SWT.CHECK);
		consumersCheck.setText("Abnehmer");
		consumersCheck.setSelection(config.isWithConsumers());
		Controls.onSelect(consumersCheck, $ -> onSelectionChanged());

		stationsCheck = new Button(group, SWT.CHECK);
		stationsCheck.setText("Hausübergabestationen");
		stationsCheck.setSelection(config.isWithStations());
		Controls.onSelect(stationsCheck, $ -> onSelectionChanged());

		pipesCheck = new Button(group, SWT.CHECK);
		pipesCheck.setText("Wärmeleitungen");
		pipesCheck.setSelection(config.isWithPipes());
		Controls.onSelect(pipesCheck, $ -> onSelectionChanged());
	}

	private void createDataModeGroup(Composite parent) {
		var group = new Group(parent, SWT.NONE);
		group.setText("Bereits vorhandene Daten");
		UI.gridData(group, true, false);
		UI.gridLayout(group, 1, 5, 10);

		updateRadio = new Button(group, SWT.RADIO);
		updateRadio.setText("aktualisieren");
		updateRadio.setSelection(config.isUpdateExisting());
		Controls.onSelect(updateRadio, $ -> onSelectionChanged());

		var appendRadio = new Button(group, SWT.RADIO);
		appendRadio.setText("ergänzen");
		appendRadio.setSelection(!config.isUpdateExisting());
		Controls.onSelect(appendRadio, $ -> onSelectionChanged());
	}

	private void createFileGroup(Composite parent) {
		var group = new Group(parent, SWT.NONE);
		group.setText("Importdatei");
		UI.gridData(group, true, false);
		UI.gridLayout(group, 2, 5, 10);

		fileText = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		UI.gridData(fileText, true, false);

		var browseBtn = new Button(group, SWT.PUSH);
		browseBtn.setText("Durchsuchen...");
		browseBtn.addListener(SWT.Selection, e -> onBrowse());
	}

	private void onBrowse() {
		var file = FileChooser.open("*.gz");
		if (file == null)
			return;
		var result = ThermosFile.readFrom(file, App.getDb());
		if (result.isError()) {
			MsgBox.error(result.error());
			return;
		}
		var tf = result.value();
		if (tf.isEmpty()) {
			MsgBox.error("Leere Datei", "Die ausgewählte Datei ist leer.");
			return;
		}
		config.withThermosFile(result.value());
		fileText.setText(file.getAbsolutePath());
		onSelectionChanged();
	}

	private void onSelectionChanged() {
		config.withConsumers(consumersCheck.getSelection());
		config.withStations(stationsCheck.getSelection());
		config.withPipes(pipesCheck.getSelection());
		config.updateExisting(updateRadio.getSelection());
		setPageComplete(config.canRunImport());
		getContainer().updateButtons();
	}

	@Override
	public boolean canFlipToNextPage() {
		if (config.thermosFile() == null)
			return false;
		return config.isWithStations() || config.isWithPipes();
	}
}
