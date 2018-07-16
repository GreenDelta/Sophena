package sophena.rcp.editors.basedata.fuels;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Fuel;
import sophena.rcp.M;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Strings;

class FuelWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Fuel fuel;

	public static int open(Fuel fuel) {
		if (fuel == null)
			return Window.CANCEL;
		FuelWizard wiz = new FuelWizard();
		wiz.setWindowTitle(M.Fuel);
		wiz.fuel = fuel;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			fuel.name = page.nameText.getText();
			fuel.description = page.descriptionText.getText();
			fuel.unit = page.unitText.getText();
			fuel.calorificValue = Texts.getDouble(page.calText);
			fuel.co2Emissions = Texts.getDouble(page.co2Text);
			fuel.primaryEnergyFactor = Texts.getDouble(page.energyFactorText);
			fuel.ashContent = Texts.getDouble(page.ashContentText);
			fuel.group = page.groupCombo.getSelected();
			return true;
		} catch (Exception e) {
			log.error("failed to set fuel data " + fuel, e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		page.setPageComplete(!fuel.isProtected);
		addPage(page);
	}

	private class Page extends WizardPage {

		private Label unitLabel;

		private Text nameText;
		private FuelGroupCombo groupCombo;
		private Text unitText;
		private Text calText;
		private Text co2Text;
		private Text energyFactorText;
		private Text ashContentText;
		private Text descriptionText;

		private Page() {
			super("FuelWizardPage", M.Fuel, null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createNameText(comp);
			groupCombo = FuelGroupCombo.on(fuel, comp);
			createUnitText(comp);
			createCalText(comp);
			createCO2Text(comp);
			createEnergyFactorText(comp);
			createAshContentText(comp);
			createDescriptionText(comp);
			validate();
		}

		private void createNameText(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			Texts.on(nameText)
					.init(fuel.name)
					.required()
					.validate(this::validate);
			UI.formLabel(comp, "");
		}

		private void createDescriptionText(Composite comp) {
			descriptionText = UI.formMultiText(comp, M.Description);
			Texts.set(descriptionText, fuel.description);
			UI.formLabel(comp, "");
		}

		private void createUnitText(Composite comp) {
			unitText = UI.formText(comp, M.Unit);
			Texts.on(unitText)
					.init(fuel.unit)
					.required()
					.onChanged((t) -> {
						String unit = t == null ? "" : t.trim();
						unitLabel.setText("kWh/" + unit);
						comp.layout();
						validate();
					});
			UI.formLabel(comp, "");
		}

		private void createCalText(Composite comp) {
			calText = UI.formText(comp, M.CalorificValue);
			Texts.on(calText)
					.init(fuel.calorificValue)
					.required()
					.decimal()
					.validate(this::validate);
			unitLabel = UI.formLabel(comp, "");
			if (fuel.unit != null)
				unitLabel.setText("kWh/" + fuel.unit);
		}

		private void createCO2Text(Composite comp) {
			co2Text = UI.formText(comp, "CO2 Emissionen");
			Texts.on(co2Text)
					.init(fuel.co2Emissions)
					.required()
					.decimal()
					.validate(this::validate);
			UI.formLabel(comp, "g CO2 äq./kWh");
		}

		private void createEnergyFactorText(Composite comp) {
			energyFactorText = UI.formText(comp, "Primärenergiefaktor");
			Texts.on(energyFactorText)
					.init(fuel.primaryEnergyFactor)
					.required()
					.decimal()
					.validate(this::validate);
			UI.filler(comp);
		}

		private void createAshContentText(Composite comp) {
			ashContentText = UI.formText(comp, "Aschegehalt");
			Texts.on(ashContentText)
					.init(fuel.ashContent)
					.decimal()
					.validate(this::validate);
			UI.formLabel(comp, "%");
		}

		private boolean validate() {
			if (Texts.isEmpty(nameText))
				return error("Es muss ein Name angegeben werden.");
			if (Strings.nullOrEmpty(unitText.getText()))
				return error("Es muss eine Einheit angegeben werden.");
			if (!Num.isNumeric(calText.getText()))
				return error("Es muss ein Heizwert angegeben werden.");
			setPageComplete(!fuel.isProtected);
			setErrorMessage(null);
			return true;
		}

		private boolean error(String message) {
			setErrorMessage(message);
			setPageComplete(false);
			return false;
		}
	}

}
