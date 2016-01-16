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
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

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
		dialog.setPageSize(150, 400);
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
			fuel.wood = false;
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
		private Text unitText;
		private Text calText;
		private Text co2Text;
		private Text descriptionText;

		private Page() {
			super("FuelWizardPage", M.Fuel, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createNameText(composite);
			createUnitText(composite);
			createCalText(composite);
			createCO2Text(composite);
			createDescriptionText(composite);
			validate();
		}

		private void createNameText(Composite composite) {
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText)
					.init(fuel.name)
					.required()
					.validate(this::validate);
			UI.formLabel(composite, "");
		}

		private void createDescriptionText(Composite composite) {
			descriptionText = UI.formMultiText(composite, M.Description);
			Texts.set(descriptionText, fuel.description);
			UI.formLabel(composite, "");
		}

		private void createUnitText(Composite composite) {
			unitText = UI.formText(composite, M.Unit);
			Texts.on(unitText)
					.init(fuel.unit)
					.required()
					.onChanged((t) -> {
						String unit = t == null ? "" : t.trim();
						unitLabel.setText("kWh/" + unit);
						composite.layout();
						validate();
					});
			UI.formLabel(composite, "");
		}

		private void createCalText(Composite composite) {
			calText = UI.formText(composite, M.CalorificValue);
			Texts.on(calText)
					.init(fuel.calorificValue)
					.required()
					.decimal()
					.validate(this::validate);
			unitLabel = UI.formLabel(composite, "");
			if (fuel.unit != null)
				unitLabel.setText("kWh/" + fuel.unit);
		}

		private void createCO2Text(Composite composite) {
			co2Text = UI.formText(composite, "CO2 Emissionen");
			Texts.on(co2Text)
					.init(fuel.co2Emissions)
					.required()
					.decimal()
					.validate(this::validate);
			UI.formLabel(composite, "g CO2 äq./kWh");
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
