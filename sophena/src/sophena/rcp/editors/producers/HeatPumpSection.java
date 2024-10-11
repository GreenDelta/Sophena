package sophena.rcp.editors.producers;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.io.ConsumerProfiles;
import sophena.io.LoadHeatPumpData;
import sophena.model.HeatPumpMode;
import sophena.model.Producer;
import sophena.model.SolarCollectorOperatingMode;
import sophena.rcp.M;
import sophena.rcp.charts.HeatPumpTemperatureChart;
import sophena.rcp.charts.ProducerProfileChart;
import sophena.rcp.colors.Colors;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class HeatPumpSection {
	private ProducerEditor editor;
	private HeatPumpTemperatureChart chart;

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
				"Wärmepumpenspezifikation");
		UI.gridLayout(section, 1);
		Composite comp = new Composite(section, SWT.NONE);
		comp.setBackground(Colors.getWhite());
		UI.gridLayout(comp, 4);		
		UI.gridData(comp, true, true);
		if(producer().heatPumpMode == null)
			producer().heatPumpMode = HeatPumpMode.OUTODOOR_TEMPERATURE_MODE;
		createHeatPumpModeRow(tk, comp);
		createHeatPumpUserTemperatureRow(tk, comp);
		createHeatPumpHourlyRow(tk, comp);
		var c = new Composite(section, SWT.NONE);
		c.setBackground(Colors.getWhite());
		UI.gridLayout(c, 1);
		UI.gridData(c, true, true);
		createHeatPumpHourlyChart(tk, c);
	}
	
	private void createHeatPumpHourlyChart(FormToolkit tk, Composite comp)
	{
		chart = new HeatPumpTemperatureChart(comp, 250);
		chart.setData(producer());
	}
	
	private void createHeatPumpModeRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.HeatPumpMode);
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 3);
		HeatPumpMode current = producer().heatPumpMode;
		Button outdoor = tk.createButton(inner, M.OutdoorMode, SWT.RADIO);
		outdoor.setSelection(current == HeatPumpMode.OUTODOOR_TEMPERATURE_MODE);
		Controls.onSelect(outdoor, e -> {
			producer().heatPumpMode = HeatPumpMode.OUTODOOR_TEMPERATURE_MODE;
			editor.setDirty();
		});
		
		Button user = tk.createButton(inner, M.UserMode, SWT.RADIO);
		user.setSelection(current == HeatPumpMode.USER_TEMPERATURE_MODE);
		Controls.onSelect(user, e -> {
			producer().heatPumpMode = HeatPumpMode.USER_TEMPERATURE_MODE;
			editor.setDirty();
		});
		
		Button hourly = tk.createButton(inner, M.HourlyMode, SWT.RADIO);
		hourly.setSelection(current == HeatPumpMode.HOURLY_TEMPERATURE_MODE);
		Controls.onSelect(hourly, e -> {
			producer().heatPumpMode = HeatPumpMode.HOURLY_TEMPERATURE_MODE;
			editor.setDirty();
		});
				
		UI.formLabel(comp, "");
		HelpLink.create(comp, tk, M.HeatPumpMode, "");
	}
	
	private void createHeatPumpUserTemperatureRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, tk, M.UserTemperatureInput);
		UI.formLabel(comp, tk, "°C");
		HelpLink.create(comp, tk, M.UserTemperatureInput, "");
		Texts.on(t).decimal().required()
				.init(producer().sourceTemperatureUser)
				.onChanged((s) -> {
					producer().sourceTemperatureUser = Texts.getDouble(t);
					editor.setDirty();
				});
	}
	
	private void createHeatPumpHourlyRow(FormToolkit tk, Composite comp)
	{
		Button btn = tk.createButton(
				comp, "Quelltemperatur Verlauf importieren", SWT.NONE);
		Controls.onSelect(btn, e -> updateHourlyTemperature());
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
