package sophena.rcp.editors.fuels;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import sophena.model.Fuel;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.UI;

class WoodFuelWizard extends Wizard {

	private Page page;
	private Fuel fuel;

	public static int open(Fuel fuel) {
		if(fuel == null)
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
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}


	private class Page extends WizardPage {


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
			validate();
		}

		private void createNameText(Composite composite) {
			Text t = UI.formText(composite, M.Name);
			t.setBackground(Colors.forRequiredField());
			if(fuel.getName() != null)
				t.setText(fuel.getName());
			UI.formLabel(composite, "");
			t.addModifyListener((e) -> {
				String name = t.getText();
				name = name == null ? "" : name.trim();
				fuel.setName(name);
				validate();
			});
		}

		private void createDescriptionText(Composite composite) {
			Text t = UI.formMultiText(composite, M.Description);
			if(fuel.getDescription() != null)
				t.setText(fuel.getDescription());
			UI.formLabel(composite, "");
			t.addModifyListener((e) ->  fuel.setDescription(t.getText()));
		}

		private void createDensityText(Composite composite) {
			Text t = UI.formText(composite, "#Dichte");
			t.setBackground(Colors.forRequiredField());
			t.setText(Numbers.toString(fuel.getDensity()));
			UI.formLabel(composite, "kg/FM");
			t.addModifyListener((e) -> {
				fuel.setDensity(Numbers.read(t.getText()));
				validate();
			});
		}

		private void createCalText(Composite composite) {
			Text t = UI.formText(composite, M.CalorificValue);
			t.setBackground(Colors.forRequiredField());
			t.setText(Numbers.toString(fuel.getCalorificValue()));
			UI.formLabel(composite, "kWh/kg atro");
			t.addModifyListener((e) -> {
				double v = Numbers.read(t.getText());
				fuel.setCalorificValue(v);
				validate();
			});
		}

		private void validate() {
			if(Strings.nullOrEmpty(fuel.getName()))
				setPageComplete(false);
		}
	}

}
