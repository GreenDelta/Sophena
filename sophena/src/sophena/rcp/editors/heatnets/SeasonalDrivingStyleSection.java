package sophena.rcp.editors.heatnets;

import java.time.MonthDay;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.math.energetic.SeasonalItem;
import sophena.model.HeatNet;
import sophena.model.Stats;
import sophena.model.TimeInterval;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MonthDayBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class SeasonalDrivingStyleSection {
	private final HeatNetEditor editor;

	private MonthDayBox startBoxWinter;
	private MonthDayBox endBoxWinter;
	private MonthDayBox startBoxSummer;
	private MonthDayBox endBoxSummer;
	private Composite inner;
	private Text tTargetChargeWinter;
	private Text tTargetChargeSummer;
	private Text tFlowTemperatureWinter;
	private Text tFlowTemperatureSummer;
	private Text tReturnTemperatureWinter;
	private Text tReturnTemperatureSummer;
	private Button useHeatingCurve;
	
	private static String summerStart = "--02-15";
	private static String summerEnd = "--09-15";
	private static String winterStart = "--11-15";
	private static String winterEnd = "--03-15";
	
	SeasonalDrivingStyleSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet heatNet() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit, M.SeasonalDrivingStyle);
		UI.gridLayout(composite, 1);
		createCheck(toolkit, composite);
		inner = toolkit.createComposite(composite);
		UI.gridLayout(inner, 5);
		createHeaderRow(toolkit, inner);
		createBoxRows(toolkit, inner);
		createTargetChargeLevelRow(toolkit, inner);
		createFlowTemperatureRow(toolkit, inner);
		createReturnTemperatureRow(toolkit, inner);
		createUseHeatingCurveCheck(toolkit, inner);
		enableControls(heatNet().isSeasonalDrivingStyle);
	}

	private void enableControls(Boolean enable) {
		tTargetChargeWinter.setEnabled(enable);
		tTargetChargeSummer.setEnabled(enable);
		tFlowTemperatureWinter.setEnabled(enable);
		tFlowTemperatureSummer.setEnabled(enable);
		tReturnTemperatureWinter.setEnabled(enable);
		tReturnTemperatureSummer.setEnabled(enable);
		startBoxWinter.setEnabled(enable);
		endBoxWinter.setEnabled(enable);
		startBoxSummer.setEnabled(enable);
		endBoxSummer.setEnabled(enable);
		useHeatingCurve.setEnabled(enable);
	}
	
	private void createUseHeatingCurveCheck(FormToolkit tk, Composite comp) {
		useHeatingCurve = tk.createButton(comp, M.UseHeatingCurve, SWT.CHECK);
		useHeatingCurve.setSelection(heatNet().useHeatingCurve);
		Controls.onSelect(useHeatingCurve, (e) -> {
			heatNet().useHeatingCurve = useHeatingCurve.getSelection();
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		HelpLink.create(comp, tk, "Heizkurve",
				H.UseHeatingCurve);
	}	
	
	private void createCheck(FormToolkit tk, Composite comp) {
		var check = tk.createButton(comp, M.Activate, SWT.CHECK);
		check.setSelection(heatNet().isSeasonalDrivingStyle);
		Controls.onSelect(check, (e) -> {
			boolean enabled = check.getSelection();
			enableControls(enabled);
			heatNet().isSeasonalDrivingStyle = enabled;
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
	}	
	
	private void createHeaderRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.Period);
		UI.formLabel(comp, tk, M.Winter);
		UI.filler(comp);
		UI.formLabel(comp, tk, M.Summer);
		UI.filler(comp);
	}	
	
	private void initBoxValue(MonthDayBox box, String monthDay) {
		if (box == null || monthDay == null)
			return;
		try {
			MonthDay value = MonthDay.parse(monthDay);
			box.select(value);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse MonthDay " + monthDay, e);
		}
	}
	
	private void createBoxRows(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.Start);
		startBoxWinter = new MonthDayBox("", comp, tk);
		TimeInterval intervalWinter = heatNet().intervalWinter;
		if (intervalWinter != null) {
			if(intervalWinter.start == null)
				intervalWinter.start = winterStart;
			if(intervalWinter.end == null)
				intervalWinter.end = winterEnd;
			initBoxValue(startBoxWinter, intervalWinter.start);
		} else {
			intervalWinter = new TimeInterval();
			intervalWinter.id = UUID.randomUUID().toString();
			intervalWinter.start = winterStart;
			intervalWinter.end = winterEnd;
			heatNet().intervalWinter = intervalWinter;
		}
		startBoxWinter.onSelect((monthDay) -> {
			TimeInterval i = heatNet().intervalWinter;
			if (i == null || monthDay == null)
				return;
			i.start = monthDay.toString();
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		UI.filler(comp);
		startBoxSummer = new MonthDayBox("", comp, tk);
		TimeInterval intervalSummer = heatNet().intervalSummer;
		if (intervalSummer != null) {
			if(intervalSummer.start == null)
				intervalSummer.start = summerStart;
			if(intervalSummer.end == null)
				intervalSummer.end = summerEnd;
			initBoxValue(startBoxSummer, intervalSummer.start);
		} else {
			intervalSummer = new TimeInterval();
			intervalSummer.id = UUID.randomUUID().toString();
			intervalSummer.start = summerStart;
			intervalSummer.end = summerEnd;
			heatNet().intervalSummer = intervalSummer;
		}
		startBoxSummer.onSelect((monthDay) -> {
			TimeInterval i = heatNet().intervalSummer;
			if (i == null || monthDay == null)
				return;
			i.start = monthDay.toString();
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		HelpLink.create(comp, tk, "Zwischenzeiten",
				H.InterpolationInfo);

		UI.formLabel(comp, tk, M.End);
		endBoxWinter = new MonthDayBox("", comp, tk);
		initBoxValue(endBoxWinter, intervalWinter.end);
		endBoxWinter.onSelect((monthDay) -> {
			TimeInterval i = heatNet().intervalWinter;
			if (i == null || monthDay == null)
				return;
			i.end = monthDay.toString();
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		UI.filler(comp);
		endBoxSummer = new MonthDayBox("", comp, tk);
		initBoxValue(endBoxSummer, intervalSummer.end);
		endBoxSummer.onSelect((monthDay) -> {
			TimeInterval i = heatNet().intervalSummer;
			if (i == null || monthDay == null)
				return;
			i.end = monthDay.toString();
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		UI.filler(comp);
	}
	
	private void createTargetChargeLevelRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.TargetChargeLevel);
		tTargetChargeWinter = UI.formText(comp, tk, null);
		Texts.on(tTargetChargeWinter).decimal().required()
		.init(heatNet().targetChargeLevelWinter)
		.onChanged((s) -> {
			heatNet().targetChargeLevelWinter = Texts.getDouble(tTargetChargeWinter);
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "%");
		tTargetChargeSummer = UI.formText(comp, tk, null);
		Texts.on(tTargetChargeSummer).decimal().required()
		.init(heatNet().targetChargeLevelSummer)
		.onChanged((s) -> {
			heatNet().targetChargeLevelSummer = Texts.getDouble(tTargetChargeSummer);
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "%");
	}
	
	private void createFlowTemperatureRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.FlowTemperature);
		tFlowTemperatureWinter = UI.formText(comp, tk, null);
		Texts.on(tFlowTemperatureWinter).decimal().required()
		.init(heatNet().flowTemperatureWinter)
		.onChanged((s) -> {
			heatNet().flowTemperatureWinter = Texts.getDouble(tFlowTemperatureWinter);
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "°C");
		tFlowTemperatureSummer = UI.formText(comp, tk, null);
		Texts.on(tFlowTemperatureSummer).decimal().required()
		.init(heatNet().flowTemperatureSummer)
		.onChanged((s) -> {
			heatNet().flowTemperatureSummer = Texts.getDouble(tFlowTemperatureSummer);
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "°C");
	}
	
	private void createReturnTemperatureRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, M.ReturnTemperature);
		tReturnTemperatureWinter = UI.formText(comp, tk, null);
		Texts.on(tReturnTemperatureWinter).decimal().required()
		.init(heatNet().returnTemperatureWinter)
		.onChanged((s) -> {
			heatNet().returnTemperatureWinter = Texts.getDouble(tReturnTemperatureWinter);
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "°C");
		tReturnTemperatureSummer = UI.formText(comp, tk, null);
		Texts.on(tReturnTemperatureSummer).decimal().required()
		.init(heatNet().returnTemperatureSummer)
		.onChanged((s) -> {
			heatNet().returnTemperatureSummer = Texts.getDouble(tReturnTemperatureSummer);
			editor.bus.notify("seasonal-driving-changed");
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "°C");
	}
}
