package sophena.rcp.editors.basedata.pipes;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.Pipe;
import sophena.model.PipeType;
import sophena.model.ProductType;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class PipeWizard implements IContent {

	private final Pipe pipe;

	private ProductWizard wizard;
	private Text materialText;
	private Combo typeCombo;
	private Text uValueText;
	private Text innerDiamText;
	private Text outerDiamText;
	private Text totalDiamText;
	private Text deliveryTypeText;
	private Text maxTempText;
	private Text maxPressureText;

	private PipeWizard(Pipe pipe) {
		this.pipe = pipe;
	}

	static int open(Pipe pipe) {
		if (pipe == null)
			return Window.CANCEL;
		PipeWizard content = new PipeWizard(pipe);
		ProductWizard w = new ProductWizard(pipe, content);
		content.wizard = w;
		w.setWindowTitle("Wärmeleitung");
		WizardDialog d = new WizardDialog(UI.shell(), w);
		return d.open();
	}

	@Override
	public void render(Composite c) {

		materialText = Texts.on(UI.formText(c, "Material"))
			.disableWhen(pipe.isProtected)
			.required()
			.validate(wizard::validate)
			.get();
		UI.filler(c);

		typeCombo = UI.formCombo(c, "Art");
		typeCombo.setItems("UNO", "DUO");
		typeCombo.select(0);
		typeCombo.setEnabled(!pipe.isProtected);
		UI.filler(c);

		uValueText = Texts.on(UI.formText(c, "U-Wert"))
			.disableWhen(pipe.isProtected)
			.required()
			.decimal()
			.validate(wizard::validate)
			.get();
		UI.formLabel(c, "W/(m*K)");

		innerDiamText = Texts.on(UI.formText(c, "Innend. Medienrohr"))
			.disableWhen(pipe.isProtected)
			.required()
			.decimal()
			.validate(wizard::validate)
			.get();
		UI.formLabel(c, "mm");

		outerDiamText = Texts.on(UI.formText(c, "Außend. Medienrohr"))
			.disableWhen(pipe.isProtected)
			.required()
			.decimal()
			.validate(wizard::validate)
			.get();
		UI.formLabel(c, "mm");

		totalDiamText = Texts.on(UI.formText(c, "Außend. Gesamt"))
			.disableWhen(pipe.isProtected)
			.required()
			.decimal()
			.validate(wizard::validate)
			.get();
		UI.formLabel(c, "mm");

		deliveryTypeText = Texts.on(UI.formText(c, "Lieferausführung"))
			.disableWhen(pipe.isProtected)
			.get();
		UI.formLabel(c, "");

		maxTempText = Texts.on(UI.formText(c, "Maximale Temperatur"))
			.disableWhen(pipe.isProtected)
			.get();
		UI.formLabel(c, "°C");

		maxPressureText = Texts.on(UI.formText(c, "Maximaler Druck"))
			.disableWhen(pipe.isProtected)
			.get();
		UI.formLabel(c, "Bar");
	}

	@Override
	public void bindToUI() {
		Texts.set(totalDiamText, pipe.totalDiameter);
		Texts.set(materialText, pipe.material);
		int idx = pipe.pipeType == PipeType.UNO ? 0 : 1;
		typeCombo.select(idx);
		Texts.set(uValueText, pipe.uValue, 4);
		Texts.set(innerDiamText, pipe.innerDiameter);
		Texts.set(outerDiamText, pipe.outerDiameter);
		Texts.set(totalDiamText, pipe.totalDiameter);
		Texts.set(deliveryTypeText, pipe.deliveryType);
		Texts.set(maxTempText, pipe.maxTemperature);
		Texts.set(maxPressureText, pipe.maxPressure);
	}

	@Override
	public void bindToModel() {
		pipe.material = materialText.getText();
		pipe.pipeType = typeCombo.getSelectionIndex() == 0 ? PipeType.UNO
				: PipeType.DUO;
		pipe.totalDiameter = Texts.getDouble(totalDiamText);
		pipe.innerDiameter = Texts.getDouble(innerDiamText);
		pipe.outerDiameter = Texts.getDouble(outerDiamText);
		pipe.maxTemperature = Texts.getDouble(maxTempText);
		pipe.maxPressure = Texts.getDouble(maxPressureText);
		pipe.outerDiameter = Texts.getDouble(outerDiamText);
		pipe.deliveryType = deliveryTypeText.getText();
		pipe.uValue = Texts.getDouble(uValueText);
	}

	@Override
	public String validate() {
		if (!Texts.hasNumber(totalDiamText))
			return ("Es muss ein Durchmesser angegeben werden.");
		if (!Texts.hasNumber(uValueText))
			return ("Es muss ein U-Wert angegeben werden.");

		return null;
	}

	@Override
	public String getPageName() {
		return "Wärmeleitung";
	}

	@Override
	public ProductType getProductType() {
		return ProductType.PIPE;
	}

}
