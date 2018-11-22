package sophena.rcp.editors.basedata.boilers;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.Labels;
import sophena.model.Boiler;
import sophena.model.ProductType;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BoilerWizard implements IContent {

	private final Boiler boiler;

	private ProductWizard wizard;
	private Text maxText;
	private Text minText;
	private Text efficiencyText;
	private Text maxElText;
	private Text minElText;
	private Text efficiencyElText;

	private BoilerWizard(Boiler boiler) {
		this.boiler = boiler;
	}

	public static int open(Boiler boiler) {
		if (boiler == null || boiler.type == null)
			return Window.CANCEL;
		BoilerWizard content = new BoilerWizard(boiler);
		ProductWizard w = new ProductWizard(boiler, content);
		content.wizard = w;
		w.setWindowTitle(Labels.get(boiler.type));
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		return dialog.open();
	}

	@Override
	public void render(Composite c) {
		if (!boiler.isCoGenPlant) {
			createMinMaxTexts(c);
			createEfficiencyText(c);
		} else {
			createMinMaxTexts(c);
			createEfficiencyText(c);
			createMinMaxElTexts(c);
			createEfficiencyElText(c);
		}

	}

	private void createMinMaxTexts(Composite c) {
		minText = UI.formText(c, "Minimale Leistung th.");
		Texts.on(minText).decimal().required().validate(wizard::validate);
		UI.formLabel(c, "kW");
		maxText = UI.formText(c, "Maximale Leistung th.");
		Texts.on(maxText).decimal().required().validate(wizard::validate);
		UI.formLabel(c, "kW");
	}

	private void createMinMaxElTexts(Composite c) {
		minElText = UI.formText(c, "Minimale Leistung el.");
		Texts.on(minElText).decimal().required().validate(wizard::validate);
		UI.formLabel(c, "kW");
		maxElText = UI.formText(c, "Maximale Leistung el.");
		Texts.on(maxElText).decimal().required().validate(wizard::validate);
		UI.formLabel(c, "kW");
	}

	private void createEfficiencyText(Composite c) {
		efficiencyText = UI.formText(c, M.EfficiencyRate + " th.");
		Texts.on(efficiencyText).decimal().required()
				.validate(wizard::validate);
		UI.formLabel(c, "%");
	}

	private void createEfficiencyElText(Composite c) {
		efficiencyElText = UI.formText(c,
				M.EfficiencyRate + " el.");
		Texts.on(efficiencyElText).decimal().required()
				.validate(wizard::validate);
		UI.formLabel(c, "%");
	}

	@Override
	public void bindToUI() {
		Texts.set(maxText, boiler.maxPower);
		Texts.set(minText, boiler.minPower);
		Texts.set(efficiencyText, boiler.efficiencyRate * 100d);
		Texts.set(maxElText, boiler.maxPowerElectric);
		Texts.set(minElText, boiler.minPowerElectric);
		Texts.set(efficiencyElText, boiler.efficiencyRateElectric * 100d);
	}

	@Override
	public void bindToModel() {
		boiler.maxPower = Texts.getDouble(maxText);
		boiler.minPower = Texts.getDouble(minText);
		boiler.efficiencyRate = Texts.getDouble(efficiencyText) / 100d;
		boiler.maxPowerElectric = Texts.getDouble(maxElText);
		boiler.minPowerElectric = Texts.getDouble(minElText);
		boiler.efficiencyRateElectric = Texts.getDouble(efficiencyElText)
				/ 100d;
	}

	@Override
	public String validate() {
		if (boiler.isCoGenPlant)
			return validCoGen();
		else
			return validBoiler();
	}

	private String validBoiler() {
		if (!Texts.hasNumber(maxText))
			return ("Es wurde keine maximale Leistung angegeben.");
		if (!Texts.hasNumber(minText))
			return ("Es wurde keine minimale Leistung angegeben");
		if (!Texts.hasPercentage(efficiencyText))
			return ("Es muss ein Wirkungsgrad zwischen 0% und 120% angegeben werden.");
		double max = Texts.getDouble(maxText);
		double min = Texts.getDouble(minText);
		if (min > max)
			return ("Die minimale Leistung ist größer als die maximale.");
		return null;
	}

	private String validCoGen() {
		String message = validBoiler();
		if (message != null)
			return message;
		if (!Texts.hasPercentage(efficiencyElText))
			return ("Es muss ein Wirkungsgrad zwischen 0% und 120% angegeben werden.");
		if (!Texts.hasNumber(maxElText))
			return ("Es wurde keine maximale elektrische Leistung angegeben.");
		if (!Texts.hasNumber(minElText))
			return ("Es wurde keine minimale elektrische Leistung angegeben");
		double maxEl = Texts.getDouble(maxElText);
		double minEl = Texts.getDouble(minElText);
		if (minEl > maxEl)
			return ("Die minimale elektrische Leistung ist größer als die maximale.");
		return null;
	}

	@Override
	public String getPageName() {
		return Labels.get(boiler.type);
	}

	@Override
	public ProductType getProductType() {
		return boiler.type;
	}

}
