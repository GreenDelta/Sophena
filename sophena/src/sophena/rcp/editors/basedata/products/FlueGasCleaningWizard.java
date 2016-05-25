package sophena.rcp.editors.basedata.products;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.FlueGasCleaning;
import sophena.model.ProductType;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class FlueGasCleaningWizard implements IContent {

	private final FlueGasCleaning cleaning;
	private ProductWizard wizard;

	private Text maxVolumeFlowText;
	private Text fuelText;
	private Text maxProducerPowerText;
	private Text maxElectricityConsumptionText;
	private Text cleaningMethodText;
	private Text cleaningTypeText;
	private Text separationEfficiencyText;

	private FlueGasCleaningWizard(FlueGasCleaning cleaning) {
		this.cleaning = cleaning;
	}

	static int open(FlueGasCleaning cleaning) {
		if (cleaning == null)
			return Window.CANCEL;
		FlueGasCleaningWizard content = new FlueGasCleaningWizard(cleaning);
		ProductWizard w = new ProductWizard(cleaning, content);
		content.wizard = w;
		w.setWindowTitle("Rauchgasreinigung");
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(200, 520);
		return d.open();
	}

	@Override
	public void render(Composite c) {

		maxVolumeFlowText = UI.formText(c, "Max. reinigbarer Volumenstrom");
		Texts.on(maxVolumeFlowText).decimal().required()
				.validate(wizard::validate);
		UI.formLabel(c, "m3/h");

		fuelText = UI.formText(c, "Brennstoff (Wärmeerzeuger)");
		Texts.on(fuelText).required().validate(wizard::validate);
		UI.filler(c);

		maxProducerPowerText = UI.formText(c, "Max. Leistung Wärmeerzeuger");
		Texts.on(maxProducerPowerText).decimal().required()
				.validate(wizard::validate);
		UI.formLabel(c, "kW");

		maxElectricityConsumptionText = UI.formText(c, "Eigenstrombedarf");
		Texts.on(maxElectricityConsumptionText).decimal();
		UI.formLabel(c, "kW");

		cleaningMethodText = UI.formText(c, "Art der Reinigung");
		UI.filler(c);

		cleaningTypeText = UI.formText(c, "Typ der Reinigung");
		UI.filler(c);

		separationEfficiencyText = UI.formText(c, "Max. Abscheidegrad");
		Texts.on(separationEfficiencyText).decimal();
		UI.formLabel(c, "%");
	}

	@Override
	public void bindToUI() {
		Texts.set(fuelText, cleaning.fuel);
		Texts.set(maxVolumeFlowText, cleaning.maxVolumeFlow);
		Texts.set(maxProducerPowerText, cleaning.maxProducerPower);
		Texts.set(maxElectricityConsumptionText,
				cleaning.maxElectricityConsumption);
		Texts.set(cleaningMethodText, cleaning.cleaningMethod);
		Texts.set(cleaningTypeText, cleaning.cleaningType);
		Texts.set(separationEfficiencyText, cleaning.separationEfficiency);
	}

	@Override
	public void bindToModel() {

		cleaning.maxVolumeFlow = Texts.getDouble(maxVolumeFlowText);
		cleaning.maxProducerPower = Texts.getDouble(maxProducerPowerText);
		cleaning.maxElectricityConsumption = Texts.getDouble(
				maxElectricityConsumptionText);
		cleaning.cleaningMethod = cleaningMethodText.getText();
		cleaning.cleaningType = cleaningTypeText.getText();
		cleaning.separationEfficiency = Texts.getDouble(separationEfficiencyText);
		cleaning.fuel = fuelText.getText();
	}

	@Override
	public String validate() {
		if (Texts.isEmpty(maxVolumeFlowText))
			return "Es muss ein maximaler Volumenstrom angegeben werden.";
		if (Texts.isEmpty(fuelText))
			return "Es muss ein Brennstoff angegeben werden.";
		if (Texts.isEmpty(maxProducerPowerText))
			return "Es muss eine maximale Leistung des "
					+ "Erzeugers angegeben werden.";
		return null;
	}

	@Override
	public String getPageName() {
		return "Rauchgasreinigung";
	}

	@Override
	public ProductType getProductType() {
		return ProductType.FLUE_GAS_CLEANING;
	}
}
