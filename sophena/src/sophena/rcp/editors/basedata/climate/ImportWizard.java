package sophena.rcp.editors.basedata.climate;

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

import sophena.io.HoursProfile;
import sophena.model.Stats;
import sophena.model.WeatherStation;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class ImportWizard extends Wizard {

	private Page page;
	private WeatherStation station;

	public static int open(WeatherStation station) {		
		if (station == null)
			return Window.CANCEL;
		ImportWizard wiz = new ImportWizard();
		wiz.setWindowTitle(M.ClimateData);
		wiz.setNeedsProgressMonitor(true);
		wiz.station = station;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		if (!page.valid())
			return false;		
		try {
			if (station.data != null && page.file == null)
				return true;
			
			double[][] allData = HoursProfile.read2(page.file);
			if(allData == null || allData[0] == null) {
				MsgBox.error(M.PlausibilityErrors, M.FileImportError);
				return false;
			}
			if (allData[0].length != Stats.HOURS)
			{
				MsgBox.error(M.PlausibilityErrors, M.FileImportCountError);
				return false;
			}
			station.data = allData[0];
			if (allData.length > 1)
				if (allData.length == 3)
				{
					if (allData[1].length != Stats.HOURS || allData[2].length != Stats.HOURS)
					{
						MsgBox.error(M.PlausibilityErrors, M.FileImportCountError);
						return false;
					}
					station.directRadiation = allData[1];
					station.diffuseRadiation = allData[2];			
				}
				else
				{
					MsgBox.error(M.PlausibilityErrors, M.FileImportCountError);
					return false;
				}
			return true;
		} catch (Exception e) {
			MsgBox.error(M.PlausibilityErrors, M.FileImportCountError);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		page.setPageComplete(!station.isProtected);
		addPage(page);
	}

	private class Page extends WizardPage {

		private File file;

		private Page() {
			super("ClimateDataImportWizardPage", M.ClimateData, null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 4);
			createNameRow(comp);
			createLongitudeRow(comp);
			createLatitudeRow(comp);
			createAltitudeRow(comp);
			createReferenceLongitudeRow(comp);
			createFileSection(comp);
		}

		private void createNameRow(Composite comp) {
			Text t = UI.formText(comp, M.Name);
			Texts.on(t).required()
				.init(station.name)
				.onChanged((s) -> {
					station.name = t.getText();
				});
			UI.filler(comp);
			UI.filler(comp);		
		}

		private void createLongitudeRow(Composite comp) {
			Text t = UI.formText(comp, M.Longitude);
			Texts.on(t).decimal().required()
				.init(station.longitude)
				.onChanged((s) -> {
					station.longitude = Texts.getDouble(t);
				});
			UI.formLabel(comp, "°");
			HelpLink.create(comp, M.Longitude, H.LongitudeInfo);			
		}
		
		private void createLatitudeRow(Composite comp) {
			Text t = UI.formText(comp, M.Latitude);
			Texts.on(t).decimal().required()
				.init(station.latitude)
				.onChanged((s) -> {
					station.latitude = Texts.getDouble(t);
				});
			UI.formLabel(comp, "°");
			HelpLink.create(comp, M.Latitude, H.LatitudeInfo);			
		}
		
		private void createAltitudeRow(Composite comp) {
			Text t = UI.formText(comp, M.Altitude);
			Texts.on(t).decimal().required()
				.init(station.altitude)
				.onChanged((s) -> {
					station.altitude = Texts.getDouble(t);
				});
			UI.formLabel(comp, "m");
			UI.filler(comp);			
		}
		
		private void createReferenceLongitudeRow(Composite comp) {
			Text t = UI.formText(comp, M.ReferenceLongitude);
			Texts.on(t).decimal().required()
				.init(station.referenceLongitude)
				.onChanged((s) -> {
					station.referenceLongitude = Texts.getDouble(t);
				});
			UI.formLabel(comp, "°");
			HelpLink.create(comp, M.ReferenceLongitude, H.ReferenceLongitudeInfo);			
		}

		private void createFileSection(Composite comp) {
			Text text = UI.formText(comp, "Datei");
			text.setEditable(false);
			Button button = new Button(comp, SWT.NONE);
			button.setImage(Icon.FILE_16.img());
			button.setText("Auswählen");
			Controls.onSelect(button, (e) -> {
				FileDialog dialog = new FileDialog(UI.shell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.csv" });
				dialog.setText(M.SelectFile);
				String path = dialog.open();
				if (path != null) {
					file = new File(path);
					text.setText(file.getName());
				}
			});
		}
		
		private boolean valid() {
			if (station.name.isEmpty()) {
				MsgBox.error(M.PlausibilityErrors, M.StationNameError);
				return false;
			}				
			if (station.longitude < -180 || station.longitude > 180) {
				MsgBox.error(M.PlausibilityErrors, M.LongitudeError);
				return false;
			}
			if (station.latitude < -180 || station.latitude > 180) {
				MsgBox.error(M.PlausibilityErrors, M.LatitudeError);
				return false;
			}
			if (station.altitude < 0 || station.altitude > 5000) {
				MsgBox.error(M.PlausibilityErrors, M.AltitudeError);
				return false;
			}
			if (station.data == null && file == null) {
				MsgBox.error(M.PlausibilityErrors, M.NoFileError);
				return false;
			}
			return true;
		}
	}
}