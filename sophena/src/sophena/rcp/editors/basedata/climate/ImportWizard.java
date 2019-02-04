package sophena.rcp.editors.basedata.climate;

import java.io.File;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import sophena.db.daos.Dao;
import sophena.io.ClimateFileReader;
import sophena.io.ClimateFileSettings;
import sophena.model.WeatherStation;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class ImportWizard extends Wizard {

	private Page page;

	public static void open() {
		ImportWizard wiz = new ImportWizard();
		wiz.setWindowTitle(M.ClimateData);
		wiz.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.open();
	}

	@Override
	public boolean performFinish() {
		if (page.file == null)
			return false;
		try {
			WeatherStation station = page.station;
			station.id = UUID.randomUUID().toString();
			ClimateFileReader reader = new ClimateFileReader(page.file,
					page.settings);
			getContainer().run(false, false, (m) -> {
				m.beginTask("Importiere", IProgressMonitor.UNKNOWN);
				reader.run();
				station.data = reader.getResult().getData();
				Dao<WeatherStation> dao = new Dao<>(
						WeatherStation.class,
						App.getDb());
				dao.insert(station);
			});
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private WeatherStation station = new WeatherStation();
		private ClimateFileSettings settings = ClimateFileSettings.getDefault();
		private File file;

		private Page() {
			super("ClimateDataImportWizardPage", M.ClimateData, null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createTextFields(comp);
			createFileSection(comp);
		}

		private void createTextFields(Composite comp) {
			Text t = UI.formText(comp, "#Name");
			t.setText("Neue Wetterstation");
			station.name = "Neue Wetterstation";
			t.addModifyListener((e) -> station.name = t.getText());
			UI.formLabel(comp, "");
			createStartText(comp);
			createEndText(comp);
		}

		private void createStartText(Composite comp) {
			Text t = UI.formText(comp, "#Startjahr");
			t.setText("1994");
			settings.setStartYear(1994);
			Texts.onInt(t, (i) -> {
				if (i != null)
					settings.setStartYear(i);
			});
			UI.formLabel(comp, "");
		}

		private void createEndText(Composite comp) {
			Text t;
			t = UI.formText(comp, "#Endjahr");
			t.setText("2013");
			settings.setStartYear(2013);
			Texts.onInt(t, (i) -> {
				if (i != null)
					settings.setEndYear(i);
			});
			UI.formLabel(comp, "");
		}

		private void createFileSection(Composite comp) {
			Text text = UI.formText(comp, "#Datei");
			text.setEditable(false);
			Button button = new Button(comp, SWT.NONE);
			button.setImage(Icon.FILE_16.img());
			button.setText("#Auswählen");
			Controls.onSelect(button, (e) -> {
				FileDialog dialog = new FileDialog(UI.shell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.txt", "*.csv" });
				dialog.setText(M.SelectFile);
				String path = dialog.open();
				if (path != null) {
					file = new File(path);
					text.setText(file.getName());
				}
			});
		}
	}
}
