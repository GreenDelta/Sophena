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
import sophena.model.WoodAmountType;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.Numbers;
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
		wiz.setWindowTitle("Boiler");
		wiz.boiler = boiler;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			boiler.setName(page.nameText.getText());
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

		private Text nameText;
		private Combo fuelCombo;
		private Text maxText;
		private Text minText;
		private Text efficiencyText;
		private Text linkText;
		private Text priceText;

		private Page() {
			super("FuelWizardPage", M.Fuel, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createNameText(composite);
			createFuelCombo(composite);
			createEfficiencyText(composite);
			createMaxText(composite);
			createMinText(composite);
			createLinkAndPriceText(composite);
			validate();
		}

		private void createNameText(Composite composite) {
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText).required().validate(this::validate);
			if (boiler.getName() != null)
				nameText.setText(boiler.getName());
			UI.formLabel(composite, "");
		}

		private void createFuelCombo(Composite composite) {
			fuelCombo = UI.formCombo(composite, M.Fuel);
			String[] items = getFuelItems();
			fuelCombo.setItems(items);
			fuelCombo.select(getFuelIndex(items));
			UI.formLabel(composite, "");
			Controls.onSelect(fuelCombo, (e) -> validate());
		}

		private String[] getFuelItems() {
			List<String> list = new ArrayList<>();
			list.add(Labels.get(WoodAmountType.CHIPS));
			list.add(Labels.get(WoodAmountType.LOGS));
			FuelDao dao = new FuelDao(App.getDb());
			for (Fuel fuel : dao.getAll()) {
				if (!fuel.isWood())
					list.add(fuel.getName());
			}
			Collections.sort(list);
			return list.toArray(new String[list.size()]);
		}

		private int getFuelIndex(String[] items) {
			if (boiler.getFuel() == null && boiler.getWoodAmountType() == null)
				return 0;
			String label = null;
			if (boiler.getWoodAmountType() != null)
				label = Labels.get(boiler.getWoodAmountType());
			else if (boiler.getFuel() != null)
				label = boiler.getFuel().getName();
			if (label == null)
				return 0;
			for (int i = 0; i < items.length; i++) {
				if (Strings.nullOrEqual(items[i], label))
					return i;
			}
			return 0;
		}

		private void createMaxText(Composite composite) {
			maxText = UI.formText(composite, "Maximale Leistung");
			Texts.on(maxText).integer().required().validate(this::validate);
			maxText.setText(Numbers.toString(boiler.getMaxPower()));
			UI.formLabel(composite, "kW");
		}

		private void createMinText(Composite composite) {
			minText = UI.formText(composite, "Minimale Leistung");
			Texts.on(minText).integer().required().validate(this::validate);
			minText.setText(Numbers.toString(boiler.getMinPower()));
			UI.formLabel(composite, "kW");
		}

		private void createEfficiencyText(Composite composite) {
			efficiencyText = UI.formText(composite, M.EfficiencyRate);
			Texts.on(efficiencyText).decimal().required()
					.validate(this::validate);
			efficiencyText.setText(Numbers.toString(boiler.getEfficiencyRate()));
			UI.formLabel(composite, "%");
		}

		private void createLinkAndPriceText(Composite composite){
			linkText = UI.formText(composite, "Web-Link");
			if(boiler.getUrl() != null)
				linkText.setText(boiler.getUrl());
			UI.formLabel(composite, "");
			priceText = UI.formText(composite, "Preis");
			Texts.on(priceText).decimal();
			if(boiler.getPurchasePrice() != null)
				priceText.setText(Numbers.toString(boiler.getPurchasePrice()));
			UI.formLabel(composite, "EUR");
		}

		private boolean validate() {
			if (Texts.isEmpty(nameText))
				return error("Es muss ein Name angegeben werden.");
			if (!Texts.hasNumber(maxText))
				return error("Es wurde keine maximale Leistung angegeben.");
			if (!Texts.hasNumber(minText))
				return error("Es wurde keine minimale Leistung angegeben");
			if(!Texts.hasPercentage(efficiencyText))
				return error("Es wurde kein Wirkungsgrad angegeben");
			double max = Texts.getDouble(maxText);
			double min = Texts.getDouble(minText);
			if (min > max)
				return error("Die minimale Leistung ist größer als die maximale.");
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
