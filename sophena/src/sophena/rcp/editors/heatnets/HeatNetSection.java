package sophena.rcp.editors.heatnets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.ProjectLoadCurve;
import sophena.model.HeatNet;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class HeatNetSection {

	private HeatNetEditor editor;
	private LoadCurveSection loadCurve;

	HeatNetSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet heatNet() {
		return editor.getHeatNet();
	}

	public void setLoadCurve(LoadCurveSection loadCurve) {
		this.loadCurve = loadCurve;
	}

	void create(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				M.HeatingNetwork);
		UI.gridLayout(composite, 3);
		createSupplyText(composite, toolkit);
		createReturnText(composite, toolkit);
		createSimFactorText(composite, toolkit);
		createBufferText(composite, toolkit);
		createLengthText(composite, toolkit);
		createLossText(composite, toolkit);
	}

	private void createSupplyText(Composite composite, FormToolkit toolkit) {
		Text t = UI.formText(composite, toolkit, "Vorlauftemperatur");
		Texts.set(t, heatNet().getSupplyTemperature());
		Texts.on(t).decimal().required().onChanged((s) -> {
			heatNet().setSupplyTemperature(Texts.getDouble(t));
			editor.setDirty();
		});
		UI.formLabel(composite, toolkit, "°C");
	}

	private void createReturnText(Composite composite, FormToolkit toolkit) {
		Text t = UI.formText(composite, toolkit, "Rücklauftemperatur");
		Texts.set(t, heatNet().getReturnTemperature());
		Texts.on(t).decimal().required().onChanged((s) -> {
			heatNet().setReturnTemperature(Texts.getDouble(t));
			editor.setDirty();
		});
		UI.formLabel(composite, toolkit, "°C");
	}

	private void createBufferText(Composite composite, FormToolkit toolkit) {
		Text t = UI.formText(composite, toolkit, "Pufferspeicher");
		Texts.set(t, heatNet().getBufferTankVolume());
		Texts.on(t).decimal().required().onChanged((s) -> {
			heatNet().setBufferTankVolume(Texts.getDouble(t));
			editor.setDirty();
		});
		UI.formLabel(composite, toolkit, "L");
	}

	private void createSimFactorText(Composite composite, FormToolkit toolkit) {
		Text t = UI.formText(composite, toolkit, "Gleichzeitigkeitsfaktor");
		Texts.set(t, heatNet().getSimultaneityFactor());
		Texts.on(t).decimal().required().onChanged((s) -> {
			heatNet().setSimultaneityFactor(Texts.getDouble(t));
			editor.setDirty();
		});
		UI.formLabel(composite, toolkit, "").setImage(Images.INFO_16.img());
	}

	private void createLengthText(Composite composite, FormToolkit toolkit) {
		Text t = UI.formText(composite, toolkit, "Länge");
		Texts.set(t, heatNet().getLength());
		Texts.on(t).decimal().required().onChanged((s) -> {
			heatNet().setLength(Texts.getDouble(t));
			editor.setDirty();
			updateLoadCurve();
		});
		UI.formLabel(composite, toolkit, "m");
	}

	private void createLossText(Composite composite, FormToolkit toolkit) {
		Text t = UI.formText(composite, toolkit, "Verlustleistung");
		Texts.set(t, heatNet().getPowerLoss());
		Texts.on(t).decimal().required().onChanged((s) -> {
			heatNet().setPowerLoss(Texts.getDouble(t));
			editor.setDirty();
			updateLoadCurve();
		});
		UI.formLabel(composite, toolkit, "W/m");
	}

	private void updateLoadCurve() {
		if (loadCurve == null)
			return;
		double[] curve = ProjectLoadCurve.getNetLoadCurve(heatNet());
		loadCurve.setData(curve);
	}

}
