package sophena.rcp.editors.consumers;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.BoilerEfficiency;
import sophena.db.daos.FuelDao;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.UI;

class ConsumptionDataWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Consumer consumer;

	public static void open(Consumer consumer) {
		if (consumer == null)
			return;
		ConsumptionDataWizard wiz = new ConsumptionDataWizard();
		wiz.setWindowTitle(M.CollectConsumptionData);
		wiz.consumer = consumer;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		dialog.open();
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

		private EntityCombo<Fuel> fuelCombo;
		private Text consumptionText;
		private Combo unitCombo;
		private Text waterText;

		private Button effiCheck;
		private Button utilCheck;
		private Text effiText;
		private Text utilText;

		private Page() {
			super("ConsumptionDataWizardPage", M.CollectConsumptionData, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createFuelCombo(composite);
			createConsumptionRow(composite);
			createWaterContentRow(composite);
			createBoilerSection(composite);
			initData();
		}

		private void createFuelCombo(Composite composite) {
			fuelCombo = new EntityCombo<>();
			fuelCombo.create(M.Fuel, composite);
			UI.formLabel(composite, "");
			fuelCombo.onSelect((f) -> {
				fuelSelected(f);
				validate();
			});
		}

		private void initData() {
			FuelDao dao = new FuelDao(App.getDb());
			List<Fuel> fuels = dao.getAll();
			Collections.sort(fuels,
					(f1, f2) -> Strings.compare(f1.getName(), f2.getName()));
			fuelCombo.setInput(fuels);
			if (fuels.size() > 0) {
				Fuel fuel = fuels.get(0);
				fuelCombo.select(fuel);
				fuelSelected(fuel);
			}
			validate();
		}

		private void createConsumptionRow(Composite composite) {
			consumptionText = UI.formText(composite, "#Verbrauch pro Jahr");
			unitCombo = new Combo(composite, SWT.READ_ONLY);
			UI.gridData(unitCombo, false, false).widthHint = 25;
			consumptionText.addModifyListener((e) -> validate());
		}

		private void createWaterContentRow(Composite composite) {
			waterText = UI.formText(composite, M.WaterContent);
			UI.formLabel(composite, "%");
			waterText.addModifyListener((e) -> validate());
		}

		private void createBoilerSection(Composite composite) {
			UI.formLabel(composite, M.BoilerEfficiency);
			effiCheck = createCheck(composite, M.EfficiencyRate);
			effiCheck.setSelection(true);
			effiText = UI.formText(composite, "");
			effiText.addModifyListener((e) -> onRateChanged());
			UI.formLabel(composite, "%");
			UI.formLabel(composite, "");
			utilCheck = createCheck(composite, M.UtilisationRate);
			utilText = UI.formText(composite, "");
			utilText.addModifyListener((e) -> onRateChanged());
			utilText.setEnabled(false);
			UI.formLabel(composite, "%");
			Controls.onSelect(effiCheck, (e) -> {
				utilCheck.setSelection(false);
				effiText.setEnabled(true);
				utilText.setEnabled(false);
			});
			Controls.onSelect(utilCheck, (e) -> {
				effiCheck.setSelection(false);
				effiText.setEnabled(false);
				utilText.setEnabled(true);
			});
		}

		private void onRateChanged() {
			int hours = consumer.getLoadHours();
			if (effiCheck.getSelection()) {
				double effi = Numbers.read(effiText.getText());
				double util = BoilerEfficiency.getUtilisationRate(effi, hours);
				utilText.setText(Numbers.toString(util));
			} else {
				double util = Numbers.read(utilText.getText());
				double effi = BoilerEfficiency.getEfficiencyRate(util, hours);
				effiText.setText(Numbers.toString(effi));
			}
		}

		private Button createCheck(Composite composite, String label) {
			Composite inner = new Composite(composite, SWT.NONE);
			UI.innerGrid(inner, 2);
			Button check = new Button(inner, SWT.RADIO);
			check.setText(label);
			UI.formLabel(inner, "").setImage(Images.INFO_16.img());
			UI.formLabel(composite, "");
			return check;
		}

		private void fuelSelected(Fuel fuel) {
			if (fuel == null)
				return;
			String[] units = getUnits(fuel);
			unitCombo.setItems(units);
			unitCombo.select(0);
			if (fuel.isWood()) {
				waterText.setEnabled(true);
				if (Strings.nullOrEmpty(waterText.getText()))
					waterText.setText("20");
			} else {
				waterText.setEnabled(false);
			}
		}

		private String[] getUnits(Fuel fuel) {
			if (fuel == null)
				return new String[0];
			if (!fuel.isWood())
				return new String[] { fuel.getUnit() };
			else
				return new String[] { "kg", "Ster", "Srm" };

		}

		private boolean validate() {
			if (fuelCombo.getSelected() == null)
				return error("#Es wurde kein Brennstoff ausgewählt.");
			else {
				setPageComplete(true);
				setErrorMessage(null);
				return true;
			}
		}

		private boolean error(String message) {
			setPageComplete(false);
			setErrorMessage(message);
			return false;
		}
	}

}
