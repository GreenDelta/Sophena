package sophena.rcp.wizards;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.Dao;
import sophena.io.ClimateFileReader;
import sophena.io.ClimateFileSettings;
import sophena.model.WeatherStation;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class ClimateDataImportWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;

	public static void open() {
		ClimateDataImportWizard wiz = new ClimateDataImportWizard();
		wiz.setWindowTitle(M.ClimateData);
		wiz.setNeedsProgressMonitor(true);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		dialog.open();
	}

	@Override
	public boolean performFinish() {
		if (page.file == null)
			return false;
		try {
			WeatherStation station = page.station;
			station.setId(UUID.randomUUID().toString());
			ClimateFileReader reader = new ClimateFileReader(page.file,
					page.settings);
			getContainer().run(false,
					false,
					(m) -> {
						m.beginTask("#Importiere", IProgressMonitor.UNKNOWN);
						reader.run();
						station.setData(reader.getResult().getData());
						// TODO: add error handling
					Dao<WeatherStation> dao = new Dao<>(WeatherStation.class,
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
			station.setName("Neue Wetterstation");
			t.addModifyListener((e) -> station.setName(t.getText()));
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
			button.setImage(Images.FILE_16.img());
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
