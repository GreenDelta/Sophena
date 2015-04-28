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
import sophena.rcp.Numbers;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

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
			fuel.setName(page.nameText.getText());
			fuel.setDescription(page.descriptionText.getText());
			fuel.setUnit(page.unitText.getText());
			fuel.setCalorificValue(Numbers.read(page.calText.getText()));
			fuel.setWood(false);
			return true;
		} catch (Exception e) {
			log.error("failed to set fuel data " + fuel, e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Label unitLabel;

		private Text nameText;
		private Text unitText;
		private Text calText;
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
			createDescriptionText(composite);
			validate();
		}

		private void createNameText(Composite composite) {
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText)
					.init(fuel.getName())
					.required()
					.onChanged(this::validate);
			UI.formLabel(composite, "");
		}

		private void createDescriptionText(Composite composite) {
			descriptionText = UI.formMultiText(composite, M.Description);
			Texts.set(descriptionText, fuel.getDescription());
			UI.formLabel(composite, "");
		}

		private void createUnitText(Composite composite) {
			unitText = UI.formText(composite, M.Unit);
			Texts.on(unitText)
					.init(fuel.getUnit())
					.required()
					.onChanged(() -> {
						String unit = unitText.getText();
						unit = unit == null ? "" : unit.trim();
						unitLabel.setText("kWh/" + fuel.getUnit());
						composite.layout();
						validate();
					});
			UI.formLabel(composite, "");
		}

		private void createCalText(Composite composite) {
			calText = UI.formText(composite, M.CalorificValue);
			Texts.on(calText)
					.init(fuel.getCalorificValue())
					.required()
					.decimal()
					.onChanged(this::validate);
			unitLabel = UI.formLabel(composite, "");
			if (fuel.getUnit() != null)
				unitLabel.setText("kWh/" + fuel.getUnit());
		}

		private boolean validate() {
			if (Texts.isEmpty(nameText))
				return error("Es muss ein Name angegeben werden.");
			if (Strings.nullOrEmpty(unitText.getText()))
				return error("Es muss eine Einheit angegeben werden.");
			if (!Numbers.isNumeric(calText.getText()))
				return error("Es muss ein Heizwert angegeben werden.");
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
