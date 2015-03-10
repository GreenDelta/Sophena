package sophena.rcp.editors.fuels;

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
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.UI;

class FuelWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Fuel fuel;

	public static int open(Fuel fuel) {
		if(fuel == null)
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
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}


	private class Page extends WizardPage {

		private Label unitLabel;

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

		private void createUnitText(Composite composite) {
			Text t = UI.formText(composite, M.Unit);
			t.setBackground(Colors.forRequiredField());
			if(fuel.getUnit() != null)
				t.setText(fuel.getUnit());
			UI.formLabel(composite, "");
			t.addModifyListener((e) -> {
				String unit = t.getText();
				unit = unit == null ? "" : unit.trim();
				fuel.setUnit(unit);
				unitLabel.setText("kWh/" + fuel.getUnit());
				composite.layout();
				validate();
			});
		}

		private void createCalText(Composite composite) {
			Text t = UI.formText(composite, M.CalorificValue);
			t.setBackground(Colors.forRequiredField());
			t.setText(Numbers.toString(fuel.getCalorificValue()));
			unitLabel = UI.formLabel(composite, "");
			if(fuel.getUnit() != null)
				unitLabel.setText("kWh/" + fuel.getUnit());
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
