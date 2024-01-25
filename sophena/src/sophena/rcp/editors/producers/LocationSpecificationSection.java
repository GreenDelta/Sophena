package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.math.CalculateModules;
import sophena.model.Producer;
import sophena.model.SolarCollectorOperatingMode;
import sophena.model.SolarCollectorSpec;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class LocationSpecificationSection {
	private ProducerEditor editor;
	private Text moduleCount;


	LocationSpecificationSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}
	
	void create(Composite body, FormToolkit tk) {
		if (producer().solarCollector == null)
			return;
		Composite comp = UI.formSection(body, tk,
				"Standortspezifikation");
		UI.gridLayout(comp, 4);		
		if (producer().solarCollectorSpec == null) {
			producer().solarCollectorSpec = new SolarCollectorSpec();
			producer().solarCollectorSpec.solarCollectorOperatingMode = SolarCollectorOperatingMode.AUTO_RADIATION;
		}
		createSolarCollectorAreaRow(tk, comp);
		createSolarCollectorModuleCountRow(tk, comp);
		createSolarCollectorAlignmentRow(tk, comp);
		createSolarCollectorTiltRow(tk, comp);
		createSolarCollectorOperatingModeRow(tk, comp);
		createSolarCollectorTempDiffernceRow(tk, comp);
		createSolarCollectorTempIncreaseRow(tk, comp);
	}
	
	private void createSolarCollectorAreaRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, M.Area);
		UI.formLabel(comp, "m2");
		HelpLink.create(comp, tk, M.Area, H.CollectorArea);
		Texts.on(t).decimal().required()
				.init(producer().solarCollectorSpec.solarCollectorArea)
				.onChanged((s) -> {
					producer().solarCollectorSpec.solarCollectorArea = Texts.getDouble(t);
					Texts.set(moduleCount, Num.intStr(CalculateModules.getCount(producer().solarCollectorSpec.solarCollectorArea, producer().solarCollector.collectorArea)));
					editor.setDirty();
				});
	}
	
	private void createSolarCollectorModuleCountRow(FormToolkit tk, Composite comp) {
		moduleCount = UI.formText(comp, tk, M.ModuleCount);
		Texts.on(moduleCount).decimal().calculated()
			.init(Num.intStr(CalculateModules.getCount(producer().solarCollectorSpec.solarCollectorArea, producer().solarCollector.collectorArea)));
		UI.formLabel(comp, "");
		UI.filler(comp);
	}
	
	private void createSolarCollectorAlignmentRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, M.Alignment);
		UI.formLabel(comp, "°");
		HelpLink.create(comp, tk, M.Alignment, H.CollectorAlignment);
		Texts.on(t).decimal().required()
				.init(producer().solarCollectorSpec.solarCollectorAlignment)
				.onChanged((s) -> {
					producer().solarCollectorSpec.solarCollectorAlignment = Texts.getDouble(t);
					editor.setDirty();
				});
	}
	
	private void createSolarCollectorTiltRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, M.Tilt);
		UI.formLabel(comp, "°");
		HelpLink.create(comp, tk, M.Tilt, H.CollectorTilt);
		Texts.on(t).decimal().required()
				.init(producer().solarCollectorSpec.solarCollectorTilt)
				.onChanged((s) -> {
					producer().solarCollectorSpec.solarCollectorTilt = Texts.getDouble(t);
					editor.setDirty();
				});
	}
	
	private void createSolarCollectorOperatingModeRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, M.OperatingMode);
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 4);
		SolarCollectorOperatingMode current = producer().solarCollectorSpec.solarCollectorOperatingMode;
		Button autoRadiation = tk.createButton(inner, M.AutoRadiation, SWT.RADIO);
		autoRadiation.setSelection(current == SolarCollectorOperatingMode.AUTO_RADIATION);
		Controls.onSelect(autoRadiation, e -> {
			producer().solarCollectorSpec.solarCollectorOperatingMode = SolarCollectorOperatingMode.AUTO_RADIATION;
			editor.setDirty();
		});
		
		Button autoSeason = tk.createButton(inner, M.AutoSeason, SWT.RADIO);
		autoSeason.setSelection(current == SolarCollectorOperatingMode.AUTO_SEASON);
		Controls.onSelect(autoSeason, e -> {
			producer().solarCollectorSpec.solarCollectorOperatingMode = SolarCollectorOperatingMode.AUTO_SEASON;
			editor.setDirty();
		});
		
		Button preheating = tk.createButton(inner, M.PreheatingMode, SWT.RADIO);
		preheating.setSelection(current == SolarCollectorOperatingMode.PREHEATING_MODE);
		Controls.onSelect(preheating, e -> {
			producer().solarCollectorSpec.solarCollectorOperatingMode = SolarCollectorOperatingMode.PREHEATING_MODE;
			editor.setDirty();
		});
				
		Button targetTemp = tk.createButton(inner, M.TargetTemperatureOperation, SWT.RADIO);
		targetTemp.setSelection(current == SolarCollectorOperatingMode.TARGET_TEMPERATURE_OPERATION);
		Controls.onSelect(targetTemp, e -> {
			producer().solarCollectorSpec.solarCollectorOperatingMode = SolarCollectorOperatingMode.TARGET_TEMPERATURE_OPERATION;
			editor.setDirty();
		});
		UI.formLabel(comp, "");
		UI.filler(comp);
	}
	
	private void createSolarCollectorTempDiffernceRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, M.TemperatureDifference);
		UI.formLabel(comp, "K");
		HelpLink.create(comp, tk, M.TemperatureDifference, H.CollectorTemperatureDifference);
		Texts.on(t).decimal().required()
				.init(producer().solarCollectorSpec.solarCollectorTemperatureDifference)
				.onChanged((s) -> {
					producer().solarCollectorSpec.solarCollectorTemperatureDifference = Texts.getDouble(t);
					editor.setDirty();
				});
	}
	
	private void createSolarCollectorTempIncreaseRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, M.TemperatureIncrease);
		UI.formLabel(comp, "K");
		HelpLink.create(comp, tk, M.TemperatureIncrease, H.CollectorTemperatureIncrease);
		Texts.on(t).decimal().required()
				.init(producer().solarCollectorSpec.solarCollectorTemperatureIncrease)
				.onChanged((s) -> {
					producer().solarCollectorSpec.solarCollectorTemperatureIncrease = Texts.getDouble(t);
					editor.setDirty();
				});
	}
}
