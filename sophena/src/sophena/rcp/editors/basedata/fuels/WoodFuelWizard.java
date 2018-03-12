package sophena.rcp.editors.basedata.fuels;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.Fuel;
import sophena.model.FuelGroup;
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
		dialog.setPageSize(150, 300);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		fuel.group = FuelGroup.WOOD;
		fuel.unit = "kg"; // TODO: default units changed for wood!
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

		private DataBinding data = new DataBinding();

		private Text nameText;
		private Text descriptionText;
		private Text densityText;
		private Text calText;
		private Text energyFactorText;
		private Text co2Text;

		private Page() {
			super("FuelWizardPage", M.WoodFuel, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createNameText(composite);
			createDensityText(composite);
			createCalText(composite);
			createCO2Text(composite);
			createEnergyFactorText(composite);
			createDescriptionText(composite);
			data.validate();
		}

		private void createNameText(Composite composite) {
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText)
					.init(fuel.name)
					.required()
					.validate(data::validate);
			UI.formLabel(composite, "");
		}

		private void createDescriptionText(Composite composite) {
			descriptionText = UI.formMultiText(composite, M.Description);
			Texts.set(descriptionText, fuel.description);
			UI.formLabel(composite, "");
		}

		private void createDensityText(Composite composite) {
			densityText = UI.formText(composite, "Dichte");
			Texts.on(densityText)
					.init(fuel.density)
					.required()
					.decimal()
					.validate(data::validate);
			UI.formLabel(composite, "kg/FM");
		}

		private void createCalText(Composite composite) {
			calText = UI.formText(composite, M.CalorificValue);
			Texts.on(calText)
					.init(fuel.calorificValue)
					.required()
					.decimal()
					.validate(data::validate);
			UI.formLabel(composite, "kWh/kg atro");
		}

		private void createCO2Text(Composite composite) {
			co2Text = UI.formText(composite, "CO2 Emissionen");
			Texts.on(co2Text)
					.init(fuel.co2Emissions)
					.required()
					.decimal()
					.validate(data::validate);
			UI.formLabel(composite, "g CO2 äq./kWh");
		}

		private void createEnergyFactorText(Composite composite) {
			energyFactorText = UI.formText(composite, "Primärenergiefaktor");
			Texts.on(energyFactorText)
					.init(fuel.primaryEnergyFactor)
					.required()
					.decimal()
					.validate(data::validate);
			UI.filler(composite);
		}

		private class DataBinding {

			void bindToModel() {
				fuel.name = nameText.getText();
				fuel.description = descriptionText.getText();
				fuel.density = Texts.getDouble(densityText);
				fuel.calorificValue = Texts.getDouble(calText);
				fuel.co2Emissions = Texts.getDouble(co2Text);
				fuel.primaryEnergyFactor = Texts.getDouble(page.energyFactorText);
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
