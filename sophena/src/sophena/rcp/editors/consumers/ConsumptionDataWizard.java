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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.FuelDao;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.model.Unit;
import sophena.rcp.App;
import sophena.rcp.M;
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
			createRadioRow(composite);
		}

		private void createFuelCombo(Composite composite) {
			EntityCombo<Fuel> combo = new EntityCombo<>();
			combo.create(M.Fuel, composite);
			FuelDao dao = new FuelDao(App.getDb());
			List<Fuel> fuels = dao.getAll();
			Collections.sort(fuels,
					(f1, f2) -> Strings.compare(f1.getName(), f2.getName()));
			combo.setInput(fuels);
			if (fuels.size() > 0)
				combo.select(fuels.get(0));
			new Label(composite, SWT.NONE);
		}

		private void createConsumptionRow(Composite composite) {
			Text t = UI.formText(composite, "#Verbrauch pro Jahr");
			EntityCombo<Unit> combo = new EntityCombo<>();
			combo.create(new Combo(composite, SWT.READ_ONLY));
		}

		private void createWaterContentRow(Composite composite) {
			Text t = UI.formText(composite, M.WaterContent);
			UI.formLabel(composite, "%");
		}

		private void createEfficiencyRow(Composite composite) {
			Text t = UI.formText(composite, M.BoilerEfficiency);
			UI.formLabel(composite, "%");
		}

		private void createRadioRow(Composite composite) {
			UI.formLabel(composite, M.BoilerEfficiency);
			Button effiButton = new Button(composite, SWT.RADIO);
			effiButton.setText(M.EfficiencyRate);
			UI.formLabel(composite, "");
			Text effiText = UI.formText(composite, "");
			UI.formLabel(composite, "%");

			UI.formLabel(composite, "");
			Button utilButton = new Button(composite, SWT.RADIO);
			utilButton.setText(M.UtilisationRate);
			UI.formLabel(composite, "");
			Text utilText = UI.formText(composite, "");
			utilText.setEnabled(false);
			UI.formLabel(composite, "%");

		}
	}

}
