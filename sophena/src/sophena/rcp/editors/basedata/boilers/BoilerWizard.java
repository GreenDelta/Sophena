package sophena.rcp.editors.basedata.boilers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.FuelDao;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.ProductType;
import sophena.model.WoodAmountType;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BoilerWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Boiler boiler;

	public static int open(Boiler boiler) {
		if (boiler == null)
			return Window.CANCEL;
		BoilerWizard wiz = new BoilerWizard();
		wiz.setWindowTitle(boiler.isCoGenPlant ? "KWK-Anlage" : "Heizkessel");
		wiz.boiler = boiler;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			page.data.bindToModel();
			return true;
		} catch (Exception e) {
			log.error("failed to set Boiler data " + boiler, e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private DataBinding data = new DataBinding();

		private Text nameText;
		private Combo fuelCombo;
		private Text maxText;
		private Text minText;
		private Text efficiencyText;
		private Text maxElText;
		private Text minElText;
		private Text efficiencyElText;
		private Text linkText;
		private Text priceText;

		private Page() {
			super("FuelWizardPage",
					boiler.isCoGenPlant ? "KWK-Anlage" : "Heizkessel", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createNameTextAndFuelCombo(composite);
			createEfficiencyText(composite);
			createMinMaxTexts(composite);
			if (boiler.isCoGenPlant) {
				createEfficiencyElText(composite);
				createMinMaxElTexts(composite);
			}
			createLinkAndPriceText(composite);
			data.bindToUI();
		}

		private void createNameTextAndFuelCombo(Composite composite) {
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(composite, "");
			fuelCombo = UI.formCombo(composite, M.Fuel);
			UI.formLabel(composite, "");
			Controls.onSelect(fuelCombo, (e) -> data.validate());
		}

		private void createMinMaxTexts(Composite composite) {
			minText = UI.formText(composite, "Minimale Leistung th.");
			Texts.on(minText).decimal().required().validate(data::validate);
			UI.formLabel(composite, "kW");
			maxText = UI.formText(composite, "Maximale Leistung th.");
			Texts.on(maxText).decimal().required().validate(data::validate);
			UI.formLabel(composite, "kW");
		}

		private void createMinMaxElTexts(Composite composite) {
			minElText = UI.formText(composite, "Minimale Leistung el.");
			Texts.on(minElText).decimal().required().validate(data::validate);
			UI.formLabel(composite, "kW");
			maxElText = UI.formText(composite, "Maximale Leistung el.");
			Texts.on(maxElText).decimal().required().validate(data::validate);
			UI.formLabel(composite, "kW");
		}

		private void createEfficiencyText(Composite composite) {
			efficiencyText = UI.formText(composite, M.EfficiencyRate + " th.");
			Texts.on(efficiencyText).decimal().required()
					.validate(data::validate);
			UI.formLabel(composite, "%");
		}

		private void createEfficiencyElText(Composite composite) {
			efficiencyElText = UI.formText(composite,
					M.EfficiencyRate + " el.");
			Texts.on(efficiencyElText).decimal().required()
					.validate(data::validate);
			UI.formLabel(composite, "%");
		}

		private void createLinkAndPriceText(Composite composite) {
			linkText = UI.formText(composite, "Web-Link");
			UI.formLabel(composite, "");
			priceText = UI.formText(composite, "Preis");
			Texts.on(priceText).decimal();
			UI.formLabel(composite, "EUR");
		}

		private class DataBinding {

			private void bindToModel() {
				boiler.name = nameText.getText();
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
				setType(wat);
				boiler.maxPower = Texts.getDouble(maxText);
				boiler.minPower = Texts.getDouble(minText);
				boiler.efficiencyRate = Texts.getDouble(efficiencyText);
				boiler.maxPowerElectric = Texts.getDouble(maxElText);
				boiler.minPowerElectric = Texts.getDouble(minElText);
				boiler.efficiencyRateElectric = Texts
						.getDouble(efficiencyElText);
				boiler.url = linkText.getText();
				if (Texts.hasNumber(priceText))
					boiler.purchasePrice = Texts.getDouble(priceText);
				else
					boiler.purchasePrice = null;
			}

			private void setType(WoodAmountType wat) {
				if (boiler.isCoGenPlant)
					boiler.type = ProductType.COGENERATION_PLANT;
				else if (wat != null)
					boiler.type = ProductType.BIOMASS_BOILER;
				else
					boiler.type = ProductType.FOSSIL_FUEL_BOILER;
			}

			private Fuel findFuel(String label) {
				FuelDao dao = new FuelDao(App.getDb());
				for (Fuel fuel : dao.getAll()) {
					if (Strings.nullOrEqual(fuel.name, label))
						return fuel;
				}
				return null;
			}

			private void bindToUI() {
				Texts.set(nameText, boiler.name);
				String[] items = getFuelItems();
				fuelCombo.setItems(items);
				fuelCombo.select(getFuelIndex(items));
				Texts.set(maxText, boiler.maxPower);
				Texts.set(minText, boiler.minPower);
				Texts.set(efficiencyText, boiler.efficiencyRate);
				Texts.set(maxElText, boiler.maxPowerElectric);
				Texts.set(minElText, boiler.minPowerElectric);
				Texts.set(efficiencyElText, boiler.efficiencyRateElectric);
				Texts.set(linkText, boiler.url);
				Texts.set(priceText, boiler.purchasePrice);
				validate();
			}

			private String[] getFuelItems() {
				List<String> list = new ArrayList<>();
				list.add(Labels.get(WoodAmountType.CHIPS));
				list.add(Labels.get(WoodAmountType.LOGS));
				FuelDao dao = new FuelDao(App.getDb());
				for (Fuel fuel : dao.getAll()) {
					if (!fuel.wood)
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

			private boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				if (!Texts.hasNumber(maxText))
					return error("Es wurde keine maximale Leistung angegeben.");
				if (!Texts.hasNumber(minText))
					return error("Es wurde keine minimale Leistung angegeben");
				if (!Texts.hasPercentage(efficiencyText))
					return error("Es wurde kein Wirkungsgrad angegeben");
				if (!Texts.hasNumber(maxElText))
					return error(
							"Es wurde keine maximale elektrische Leistung angegeben.");
				if (!Texts.hasNumber(minElText))
					return error(
							"Es wurde keine minimale elektrische Leistung angegeben");
				if (!Texts.hasPercentage(efficiencyElText))
					return error(
							"Es wurde kein elektrischer Wirkungsgrad angegeben");
				double max = Texts.getDouble(maxText);
				double min = Texts.getDouble(minText);
				double maxEl = Texts.getDouble(maxElText);
				double minEl = Texts.getDouble(minElText);
				if (min > max)
					return error(
							"Die minimale Leistung ist größer als die maximale.");
				if (minEl > maxEl)
					return error(
							"Die minimale elektrische Leistung ist größer als die maximale.");
				else {
					setPageComplete(true);
					setErrorMessage(null);
					return true;
				}
			}

			private boolean error(String message) {
				setErrorMessage(message);
				setPageComplete(false);
				return false;
			}

		}
	}
}
