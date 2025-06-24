package sophena.rcp.editors.producers;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.io.LoadHeatPumpData;
import sophena.model.HeatPumpMode;
import sophena.model.Producer;
import sophena.rcp.M;
import sophena.rcp.charts.HeatPumpTemperatureChart;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class HeatPumpSection {
	private ProducerEditor editor;
	private HeatPumpTemperatureChart chart;
	private Button btnImport;
	private Text userinput;
	private Composite c;
	private GridData dataC;
	private Composite cUser;
	private GridData dataUser;
	
	HeatPumpSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}
	
	void create(Composite body, FormToolkit tk) {
		if (producer().heatPump == null)
			return;
		Composite section = UI.formSection(body, tk,
				"Wärmequellenspezifikation");
		UI.gridLayout(section, 1);
		Composite comp = new Composite(section, SWT.NONE);
		comp.setBackground(Colors.getWhite());
		UI.gridLayout(comp, 4);		
		UI.gridData(comp, true, true);
		createHeatPumpModeRow(tk, comp);
		
		cUser = new Composite(section, SWT.NONE);
		cUser.setBackground(Colors.getWhite());
		UI.gridLayout(cUser, 4);		
		dataUser = new GridData(SWT.FILL, SWT.FILL, false, false);
		cUser.setLayoutData(dataUser);
		createHeatPumpUserTemperatureRow(tk, cUser);
		
		c = new Composite(section, SWT.NONE);
		c.setBackground(Colors.getWhite());
		UI.gridLayout(c, 1);		
		dataC = new GridData(SWT.FILL, SWT.FILL, false, false);
	    c.setLayoutData(dataC);
	    createHeatPumpHourlyRow(tk, c);
		createHeatPumpHourlyChart(tk, c);
		updateControls();
	}
	
	private void createHeatPumpHourlyChart(FormToolkit tk, Composite comp)
	{
		chart = new HeatPumpTemperatureChart(comp, 250);
		chart.setData(producer());
	}
	
	private void createHeatPumpModeRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.HeatPumpMode);
		UI.gridLayout(comp, 1);
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 3);
		HeatPumpMode current = producer().heatPumpMode;
		if(producer().productGroup.name.contains("Luft"))
		{
			Button outdoor = tk.createButton(inner, M.OutdoorMode, SWT.RADIO);
			outdoor.setSelection(current == HeatPumpMode.OUTODOOR_TEMPERATURE_MODE);
			Controls.onSelect(outdoor, e -> {
				producer().heatPumpMode = HeatPumpMode.OUTODOOR_TEMPERATURE_MODE;
				editor.setDirty();
				updateControls();
			});
		}
		else
		{
			Button user = tk.createButton(inner, M.UserMode, SWT.RADIO);
			user.setSelection(current == HeatPumpMode.USER_TEMPERATURE_MODE);
			Controls.onSelect(user, e -> {
				producer().heatPumpMode = HeatPumpMode.USER_TEMPERATURE_MODE;
				editor.setDirty();
				updateControls();
			});
		}
		Button hourly = tk.createButton(inner, M.HourlyMode, SWT.RADIO);
		hourly.setSelection(current == HeatPumpMode.HOURLY_TEMPERATURE_MODE);
		Controls.onSelect(hourly, e -> {
			producer().heatPumpMode = HeatPumpMode.HOURLY_TEMPERATURE_MODE;
			editor.setDirty();
			updateControls();
		});	
		//HelpLink.create(inner, tk, M.HeatPumpMode, "");
	}
	
	private void updateControls()
	{
		switch(producer().heatPumpMode)
		{
		case USER_TEMPERATURE_MODE:
			cUser.setVisible(true);
			dataUser.exclude = false;
			cUser.requestLayout();
			c.setVisible(false);
			dataC.exclude = true;	
			c.requestLayout();
			break;
		case HOURLY_TEMPERATURE_MODE:
			cUser.setVisible(false);
			dataUser.exclude = true;
			cUser.requestLayout();
			c.setVisible(true);
			dataC.exclude = false;	
			c.requestLayout();
			break;
		default:
			cUser.setVisible(false);
			dataUser.exclude = true;
			cUser.requestLayout();
			c.setVisible(false);
			dataC.exclude = true;	
			c.requestLayout();
			break;
		}
		
	}
	
	private void createHeatPumpUserTemperatureRow(FormToolkit tk, Composite comp) {
		userinput = UI.formText(comp, tk, M.UserTemperatureInput);
		UI.formLabel(comp, tk, "°C");		
		Texts.on(userinput).decimal().required()
				.init(producer().sourceTemperatureUser)
				.onChanged((s) -> {
					producer().sourceTemperatureUser = Texts.getDouble(userinput);
					editor.setDirty();
				});
	}
	
	private void createHeatPumpHourlyRow(FormToolkit tk, Composite comp)
	{
		btnImport = tk.createButton(
				comp, "Temperaturverlauf importieren", SWT.NONE);
		Controls.onSelect(btnImport, e -> updateHourlyTemperature());
	}
	
	private void updateHourlyTemperature() {
		File f = FileChooser.open("*.csv", "*.txt");
		if (f == null)
			return;
		try {
			var r = LoadHeatPumpData.readHourlyTemperature(f);

			// check error
			if (r.isError()) {
				MsgBox.error(r.message().orElse(
						"Fehler beim Lesen der Datei"));
				return;
			}

			// show warnings
			if (r.isWarning()) {
				MsgBox.warn(r.message().orElse(
						"Die Datei enthält Formatfehler"));
			}
			producer().sourceTemperatureHourly = r.get();
			chart.setData(producer());
		} catch (Exception e) {
			MsgBox.error("Datei konnte nicht gelesen werden",
					e.getMessage());
			Log.error(this, "Failed to read hourly data file " + f, e);
		}
		editor.setDirty();
	}
}
