package sophena.rcp.editors.basedata.buffers;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.BufferTank;
import sophena.model.ProductType;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BufferTankWizard implements IContent {

	private final BufferTank buffer;

	private ProductWizard wizard;
	private Text volText;
	private Text diameterText;
	private Text heightText;
	private Text insulationText;

	private BufferTankWizard(BufferTank buffer) {
		this.buffer = buffer;
	}

	static int open(BufferTank buffer) {
		if (buffer == null)
			return Window.CANCEL;
		BufferTankWizard content = new BufferTankWizard(buffer);
		ProductWizard w = new ProductWizard(buffer, content);
		content.wizard = w;
		w.setWindowTitle("Pufferspeicher");
		WizardDialog d = new WizardDialog(UI.shell(), w);
		return d.open();
	}

	@Override
	public void render(Composite c) {
		volText = UI.formText(c, "Volumen");
		Texts.on(volText).required().decimal().validate(wizard::validate);
		UI.formLabel(c, "L");

		diameterText = UI.formText(c, "Durchmesser");
		Texts.on(diameterText).decimal();
		UI.formLabel(c, "mm");

		heightText = UI.formText(c, "Höhe");
		Texts.on(diameterText).decimal();
		UI.formLabel(c, "mm");

		insulationText = UI.formText(c, "Isolierung");
		Texts.on(insulationText).decimal();
		UI.formLabel(c, "mm");
	}

	@Override
	public void bindToUI() {
		Texts.set(volText, buffer.volume);
		Texts.set(diameterText, buffer.diameter);
		Texts.set(heightText, buffer.height);
		Texts.set(insulationText, buffer.insulationThickness);
	}

	@Override
	public void bindToModel() {
		buffer.volume = Texts.getDouble(volText);
		buffer.diameter = Texts.getDouble(diameterText);
		buffer.height = Texts.getDouble(heightText);
		buffer.insulationThickness = Texts.getDouble(insulationText);
	}

	@Override
	public String validate() {
		if (!Texts.hasNumber(volText))
			return "Es muss ein Volumen angegeben werden.";
		return null;
	}

	@Override
	public String getPageName() {
		return "Pufferspeicher";
	}

	@Override
	public ProductType getProductType() {
		return ProductType.BUFFER_TANK;
	}
}
