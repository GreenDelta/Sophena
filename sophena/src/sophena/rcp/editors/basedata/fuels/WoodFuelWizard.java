package sophena.rcp.editors.basedata.fuels;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.Fuel;
import sophena.rcp.M;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class WoodFuelWizard extends Wizard {

	private Page page;
	private Fuel fuel;

	public static int open(Fuel fuel) {
		if (fuel == null)
			return Window.CANCEL;
		WoodFuelWizard wiz = new WoodFuelWizard();
		wiz.setWindowTitle(M.WoodFuel);
		wiz.fuel = fuel;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		page.data.bindToModel();
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		page.setPageComplete(!fuel.isProtected);
		addPage(page);
	}

	private class Page extends WizardPage {

		private final DataBinding data = new DataBinding();

		private Text nameText;
		private Text descriptionText;
		private Text densityText;
		private Text calText;
		private Text energyFactorText;
		private Text co2Text;
		private Text ashContentText;

		private Page() {
			super("FuelWizardPage", M.WoodFuel, null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createNameText(comp);
			createDensityText(comp);
			createCalText(comp);
			createCO2Text(comp);
			createEnergyFactorText(comp);
			createAshContentText(comp);
			createDescriptionText(comp);
			data.validate();
		}

		private void createNameText(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			nameText.setEditable(!fuel.isProtected);
			Texts.on(nameText)
					.init(fuel.name)
					.required()
					.validate(data::validate);
			UI.formLabel(comp, "");
		}

		private void createDescriptionText(Composite comp) {
			descriptionText = UI.formMultiText(comp, M.Description);
			descriptionText.setEditable(!fuel.isProtected);
			Texts.set(descriptionText, fuel.description);
			UI.formLabel(comp, "");
		}

		private void createDensityText(Composite comp) {
			densityText = UI.formText(comp, "Dichte");
			densityText.setEditable(!fuel.isProtected);
			Texts.on(densityText)
					.init(fuel.density)
					.required()
					.decimal()
					.validate(data::validate);
			UI.formLabel(comp, "kg/FM");
		}

		private void createCalText(Composite comp) {
			calText = UI.formText(comp, M.CalorificValue);
			calText.setEditable(!fuel.isProtected);
			Texts.on(calText)
					.init(fuel.calorificValue)
					.required()
					.decimal()
					.validate(data::validate);
			UI.formLabel(comp, "kWh/" + fuel.unit);
		}

		private void createCO2Text(Composite comp) {
			co2Text = UI.formText(comp, "CO2 Emissionen");
			co2Text.setEditable(!fuel.isProtected);
			Texts.on(co2Text)
					.init(fuel.co2Emissions)
					.required()
					.decimal()
					.validate(data::validate);
			UI.formLabel(comp, "g CO2 äq./kWh");
		}

		private void createEnergyFactorText(Composite comp) {
			energyFactorText = UI.formText(comp, "Primärenergiefaktor");
			energyFactorText.setEditable(!fuel.isProtected);
			Texts.on(energyFactorText)
					.init(fuel.primaryEnergyFactor)
					.required()
					.decimal()
					.validate(data::validate);
			UI.filler(comp);
		}

		private void createAshContentText(Composite comp) {
			ashContentText = UI.formText(comp, "Aschegehalt");
			ashContentText.setEditable(!fuel.isProtected);
			Texts.on(ashContentText)
					.init(fuel.ashContent)
					.decimal()
					.validate(data::validate);
			UI.formLabel(comp, "%");
		}

		private class DataBinding {

			void bindToModel() {
				fuel.name = nameText.getText();
				fuel.description = descriptionText.getText();
				fuel.density = Texts.getDouble(densityText);
				fuel.calorificValue = Texts.getDouble(calText);
				fuel.co2Emissions = Texts.getDouble(co2Text);
				fuel.primaryEnergyFactor = Texts
						.getDouble(page.energyFactorText);
				fuel.ashContent = Texts.getDouble(page.ashContentText);
			}

			private boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
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
}
