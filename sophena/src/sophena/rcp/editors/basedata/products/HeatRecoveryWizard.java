package sophena.rcp.editors.basedata.products;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.HeatRecovery;
import sophena.model.ProductType;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class HeatRecoveryWizard implements IContent {

	private HeatRecovery recovery;
	private ProductWizard wizard;

	private Text fuelText;
	private Text powerText;
	private Text typeText;
	private Text producerPowerText;

	private HeatRecoveryWizard(HeatRecovery recovery) {
		this.recovery = recovery;
	}

	static int open(HeatRecovery recovery) {
		if (recovery == null)
			return Window.CANCEL;
		HeatRecoveryWizard content = new HeatRecoveryWizard(recovery);
		ProductWizard w = new ProductWizard(recovery, content);
		content.wizard = w;
		w.setWindowTitle("Wärmerückgewinnung");
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(180, 410);
		return d.open();
	}

	@Override
	public void render(Composite c) {
		powerText = UI.formText(c, "Thermische Leistung");
		Texts.on(powerText).decimal().required().validate(wizard::validate);
		UI.formLabel(c, "kW");

		typeText = UI.formText(c, "Art des Wärmeerzeugers");
		Texts.on(typeText).required().validate(wizard::validate);
		UI.filler(c);

		fuelText = UI.formText(c, "Brennstoff (Wärmeerzeuger)");
		Texts.on(fuelText).required().validate(wizard::validate);
		UI.filler(c);

		producerPowerText = UI.formText(c, "Leistung des Wärmeerzeugers");
		Texts.on(producerPowerText).decimal().required()
				.validate(wizard::validate);
		UI.formLabel(c, "kW");
	}

	@Override
	public void bindToUI() {
		Texts.set(powerText, recovery.power);
		Texts.set(typeText, recovery.heatRecoveryType);
		Texts.set(producerPowerText, recovery.producerPower);
		Texts.set(fuelText, recovery.fuel);
	}

	@Override
	public void bindToModel() {
		recovery.power = Texts.getDouble(powerText);
		recovery.heatRecoveryType = typeText.getText();
		recovery.producerPower = Texts.getDouble(producerPowerText);
		recovery.fuel = fuelText.getText();
	}

	@Override
	public String validate() {
		if (Texts.isEmpty(powerText))
			return "Es muss ein Name angegeben werden.";
		if (Texts.isEmpty(typeText))
			return "Es muss eine Art angegeben werden.";
		if (Texts.isEmpty(fuelText))
			return "Es muss ein Brennstoff angegeben werden.";
		if (Texts.isEmpty(powerText))
			return "Es muss eine maximale Leistung des "
					+ "Erzeugers angegeben werden.";
		return null;
	}

	@Override
	public String getPageName() {
		return "Wärmerückgewinnung";
	}

	@Override
	public ProductType getProductType() {
		return ProductType.HEAT_RECOVERY;
	}
}
