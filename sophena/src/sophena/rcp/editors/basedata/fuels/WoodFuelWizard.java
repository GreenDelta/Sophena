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
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		fuel.setWood(true);
		fuel.setUnit("kg");
		page.data.bindToModel();
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private DataBinding data = new DataBinding();

		private Text nameText;
		private Text descriptionText;
		private Text densityText;
		private Text calText;

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
			createDescriptionText(composite);
			data.validate();
		}

		private void createNameText(Composite composite) {
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText)
					.init(fuel.getName())
					.required()
					.onChanged(data::validate);
			UI.formLabel(composite, "");
		}

		private void createDescriptionText(Composite composite) {
			descriptionText = UI.formMultiText(composite, M.Description);
			Texts.set(descriptionText, fuel.getDescription());
			UI.formLabel(composite, "");
		}

		private void createDensityText(Composite composite) {
			densityText = UI.formText(composite, "Dichte");
			Texts.on(densityText)
					.init(fuel.getDensity())
					.required()
					.decimal()
					.onChanged(data::validate);
			UI.formLabel(composite, "kg/FM");
		}

		private void createCalText(Composite composite) {
			calText = UI.formText(composite, M.CalorificValue);
			Texts.on(calText)
					.init(fuel.getCalorificValue())
					.required()
					.decimal()
					.onChanged(data::validate);
			UI.formLabel(composite, "kWh/kg atro");
		}

		private class DataBinding {

			void bindToModel() {
				fuel.setName(nameText.getText());
				fuel.setDescription(descriptionText.getText());
				fuel.setDensity(Texts.getDouble(densityText));
				fuel.setCalorificValue(Texts.getDouble(calText));
			}

			private boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				if (!Texts.hasNumber(densityText))
					return error("Die Dichte muss numerisch sein");
				if (!Texts.hasNumber(calText))
					return error("Der Heizwert muss numerisch sein");
				setPageComplete(true);
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
