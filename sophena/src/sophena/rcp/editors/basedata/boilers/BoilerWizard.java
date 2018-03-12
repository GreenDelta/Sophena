package sophena.rcp.editors.basedata.boilers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.db.daos.FuelDao;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.ProductType;
import sophena.model.WoodAmountType;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BoilerWizard implements IContent {

	private final Boiler boiler;

	private ProductWizard wizard;
	private Combo fuelCombo;
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
		if (boiler.isCoGenPlant)
			dialog.setPageSize(150, 500);
		else
			dialog.setPageSize(150, 410);
		return dialog.open();
	}

	@Override
	public void render(Composite c) {
		if (!boiler.isCoGenPlant) {
			createFuelCombo(c);
			createMinMaxTexts(c);
			createEfficiencyText(c);
		} else {
			createFuelCombo(c);
			createMinMaxTexts(c);
			createEfficiencyText(c);
			createMinMaxElTexts(c);
			createEfficiencyElText(c);
		}

	}

	private void createFuelCombo(Composite c) {
		fuelCombo = UI.formCombo(c, M.Fuel);
		UI.formLabel(c, "");
		Controls.onSelect(fuelCombo, (e) -> wizard.validate());
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
		String[] items = getFuelItems();
		fuelCombo.setItems(items);
		fuelCombo.select(getFuelIndex(items));
		Texts.set(maxText, boiler.maxPower);
		Texts.set(minText, boiler.minPower);
		Texts.set(efficiencyText, boiler.efficiencyRate * 100d);
		Texts.set(maxElText, boiler.maxPowerElectric);
		Texts.set(minElText, boiler.minPowerElectric);
		Texts.set(efficiencyElText, boiler.efficiencyRateElectric * 100d);
	}

	private String[] getFuelItems() {
		List<String> list = new ArrayList<>();
		list.add(Labels.get(WoodAmountType.CHIPS));
		list.add(Labels.get(WoodAmountType.LOGS));
		FuelDao dao = new FuelDao(App.getDb());
		for (Fuel fuel : dao.getAll()) {
			if (!fuel.isWood())
				list.add(fuel.name);
		}
		Collections.sort(list);
		return list.toArray(new String[list.size()]);
	}

	private int getFuelIndex(String[] items) {
		if (boiler.fuel == null && boiler.woodAmountType == null)
			return 0;
		String label = null;
		if (boiler.woodAmountType != null)
			label = Labels.get(boiler.woodAmountType);
		else if (boiler.fuel != null)
			label = boiler.fuel.name;
		if (label == null)
			return 0;
		for (int i = 0; i < items.length; i++) {
			if (Strings.nullOrEqual(items[i], label))
				return i;
		}
		return 0;
	}

	@Override
	public void bindToModel() {

		int idx = fuelCombo.getSelectionIndex();
		String label = fuelCombo.getItem(idx);
		WoodAmountType wat = Labels.getWoodAmountType(label);
		if (wat != null) {
			boiler.fuel = null;
			boiler.woodAmountType = wat;
		} else {
			boiler.fuel = findFuel(label);
			boiler.woodAmountType = null;
		}
		boiler.maxPower = Texts.getDouble(maxText);
		boiler.minPower = Texts.getDouble(minText);
		boiler.efficiencyRate = Texts.getDouble(efficiencyText) / 100d;
		boiler.maxPowerElectric = Texts.getDouble(maxElText);
		boiler.minPowerElectric = Texts.getDouble(minElText);
		boiler.efficiencyRateElectric = Texts.getDouble(efficiencyElText) / 100d;
	}

	private Fuel findFuel(String label) {
		FuelDao dao = new FuelDao(App.getDb());
		for (Fuel fuel : dao.getAll()) {
			if (Strings.nullOrEqual(fuel.name, label))
				return fuel;
		}
		return null;
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
