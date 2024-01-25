package sophena.rcp.editors.heatnets;

import java.time.MonthDay;
import java.util.UUID;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.HeatNet;
import sophena.model.TimeInterval;
import sophena.rcp.M;
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
		inner.setEnabled(heatNet().isSeasonalDrivingStyle);
	}

	private void createCheck(FormToolkit tk, Composite comp) {
		var check = tk.createButton(comp, M.Activate, SWT.CHECK);
		check.setSelection(heatNet().isSeasonalDrivingStyle);
		Controls.onSelect(check, (e) -> {
			boolean enabled = check.getSelection();
			inner.setEnabled(enabled);
			heatNet().isSeasonalDrivingStyle = enabled;
			editor.setDirty();
		});
	}	
	
	private void createHeaderRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, M.Period);
		UI.formLabel(comp, M.Winter);
		UI.filler(comp);
		UI.formLabel(comp, M.Summer);
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
		UI.formLabel(comp, M.Start);
		startBoxWinter = new MonthDayBox("", comp, tk);
		TimeInterval intervalWinter = heatNet().intervalWinter;
		if (intervalWinter != null) {
			initBoxValue(startBoxWinter, intervalWinter.start);
		} else {
			intervalWinter = new TimeInterval();
			intervalWinter.id = UUID.randomUUID().toString();
			heatNet().intervalWinter = intervalWinter;
		}
		startBoxWinter.onSelect((monthDay) -> {
			TimeInterval i = heatNet().intervalWinter;
			if (i == null || monthDay == null)
				return;
			i.start = monthDay.toString();
			editor.setDirty();
		});
		UI.filler(comp);
		startBoxSummer = new MonthDayBox("", comp, tk);
		TimeInterval intervalSummer = heatNet().intervalSummer;
		if (intervalSummer != null) {
			initBoxValue(startBoxSummer, intervalSummer.start);
		} else {
			intervalSummer = new TimeInterval();
			intervalSummer.id = UUID.randomUUID().toString();
			heatNet().intervalSummer = intervalSummer;
		}
		startBoxSummer.onSelect((monthDay) -> {
			TimeInterval i = heatNet().intervalSummer;
			if (i == null || monthDay == null)
				return;
			i.start = monthDay.toString();
			editor.setDirty();
		});
		UI.filler(comp);

		UI.formLabel(comp, M.End);
		endBoxWinter = new MonthDayBox("", comp, tk);
		initBoxValue(endBoxWinter, intervalWinter.end);
		endBoxWinter.onSelect((monthDay) -> {
			TimeInterval i = heatNet().intervalWinter;
			if (i == null || monthDay == null)
				return;
			i.end = monthDay.toString();
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
			editor.setDirty();
		});
		UI.filler(comp);
	}
	
	private void createTargetChargeLevelRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, M.TargetChargeLevel);
		Text t = UI.formText(comp, tk, null);
		Texts.on(t).decimal().required()
		.init(heatNet().targetChargeLevelWinter)
		.onChanged((s) -> {
			heatNet().targetChargeLevelWinter = Texts.getDouble(t);
			editor.setDirty();
		});
		UI.formLabel(comp, "%");
		Text tSummer = UI.formText(comp, tk, null);
		Texts.on(tSummer).decimal().required()
		.init(heatNet().targetChargeLevelSummer)
		.onChanged((s) -> {
			heatNet().targetChargeLevelSummer = Texts.getDouble(tSummer);
			editor.setDirty();
		});
		UI.formLabel(comp, "%");
	}
	
	private void createFlowTemperatureRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, M.FlowTemperature);
		Text t = UI.formText(comp, tk, null);
		Texts.on(t).decimal().required()
		.init(heatNet().flowTemperatureWinter)
		.onChanged((s) -> {
			heatNet().flowTemperatureWinter = Texts.getDouble(t);
			editor.setDirty();
		});
		UI.formLabel(comp, "°C");
		Text tSummer = UI.formText(comp, tk, null);
		Texts.on(tSummer).decimal().required()
		.init(heatNet().flowTemperatureSummer)
		.onChanged((s) -> {
			heatNet().flowTemperatureSummer = Texts.getDouble(tSummer);
			editor.setDirty();
		});
		UI.formLabel(comp, "°C");
	}
	
	private void createReturnTemperatureRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, M.ReturnTemperature);
		Text t = UI.formText(comp, tk, null);
		Texts.on(t).decimal().required()
		.init(heatNet().returnTemperatureWinter)
		.onChanged((s) -> {
			heatNet().returnTemperatureWinter = Texts.getDouble(t);
			editor.setDirty();
		});
		UI.formLabel(comp, "°C");
		Text tSummer = UI.formText(comp, tk, null);
		Texts.on(tSummer).decimal().required()
		.init(heatNet().returnTemperatureSummer)
		.onChanged((s) -> {
			heatNet().returnTemperatureSummer = Texts.getDouble(tSummer);
			editor.setDirty();
		});
		UI.formLabel(comp, "°C");
	}
}
