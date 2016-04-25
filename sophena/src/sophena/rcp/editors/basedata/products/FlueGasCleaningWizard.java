package sophena.rcp.editors.basedata.products;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.db.daos.ProductGroupDao;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.TransferStation;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class FlueGasCleaningWizard extends Wizard {

	private FlueGasCleaning cleaning;
	private Page page;

	static int open(FlueGasCleaning cleaning) {
		if (cleaning == null)
			return Window.CANCEL;
		FlueGasCleaningWizard w = new FlueGasCleaningWizard();
		w.setWindowTitle("Rauchgasreinigung");
		w.cleaning = cleaning;
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(180, 700);
		return d.open();
	}

	@Override
	public boolean performFinish() {
		page.data.bindToModel();
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		DataBinding data = new DataBinding();

		Text nameText;
		EntityCombo<ProductGroup> groupCombo;
		Text urlText;
		Text priceText;

        // TODO: fuel

		private Text typeText;
		private Text maxVolumeFlowText;
		private Text maxProducerPowerText;
		private Text maxElectricityConsumptionText;
		private Text cleaningMethodText;
		private Text cleaningTypeText;
        private Text separationEfficiencyText;
		private Text descriptionTextText;

		Page() {
			super("FlueGasCleaningPage", "Rauchgasreinigung", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			nameText = UI.formText(c, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.filler(c);

			createGroupCombo(c);

			typeText = UI.formText(c, "Art");
			Texts.on(typeText).required();
			UI.filler(c);

			maxVolumeFlowText = UI.formText(c, "Max. reinigbarer Volumenstrom");
			Texts.on(maxVolumeFlowText).decimal().required();
			UI.formLabel(c, "m3/h");

			maxProducerPowerText = UI.formText(c, "Max. Leistung Wärmeerzeuger");
			Texts.on(maxProducerPowerText).decimal().required();
			UI.formLabel(c, "kW");

			maxElectricityConsumptionText = UI.formText(c, "Eigenstrombedarf");
			Texts.on(maxElectricityConsumptionText).decimal();
			UI.formLabel(c, "kW");

			cleaningMethodText = UI.formMultiText(c, "Art der Reinigung");
			UI.filler(c);

			cleaningTypeText = UI.formMultiText(c, "Typ der Reinigung");
			UI.filler(c);

            separationEfficiencyText = UI.formMultiText(c, "Max. Abscheidegrad");
			Texts.on(separationEfficiencyText).decimal();
			UI.formLabel(c, "%");

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.filler(c);

			urlText = UI.formText(c, "Web-Link");
			UI.filler(c);

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR");

			data.bindToUI();
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(ProductType.FLUE_GAS_CLEANING);
			Sorters.byName(list);
			groupCombo.setInput(list);
			UI.formLabel(c, "");
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, cleaning.name);
				groupCombo.select(cleaning.group);
				Texts.set(urlText, cleaning.url);                
				Texts.set(priceText, cleaning.purchasePrice);
                Text.set(typeText, cleaning.flueGasCleaningType);
                Text.set(maxVolumeFlowText, cleaning.maxVolumeFlow);
                Text.set(maxProducerPowerText, cleaning.maxProducerPower);
                Text.set(maxElectricityConsumptionText, cleaning.maxElectricityConsumption);
                Text.set(cleaningMethodText, cleaning.cleaningMethod);
                Text.set(cleaningTypeText, cleaning.cleaningType);
                Text.set(separationEfficiencyText, cleaning.separationEfficiency);                                
				Texts.set(descriptionText, cleaning.description);
			}

			void bindToModel() {
				cleaning.name = nameText.getText();
				cleaning.group = groupCombo.getSelected();
				cleaning.url = urlText.getText();
				if (Texts.hasNumber(priceText))
					cleaning.purchasePrice = Texts.getDouble(priceText);
                cleaning.flueGasCleaningType = typeText.getText();
                cleaning.maxVolumeFlow = Texts.getDouble(maxVolumeFlowText);
                cleaning.maxProducerPower = Texts.getDouble(maxProducerPowerText);
                cleaning.maxElectricityConsumption = Texts.getDouble(maxElectricityConsumptionText);
                cleaning.cleaningMethod = cleaningMethodText.getText();
                cleaning.cleaningType = cleaningTypeText.getText();
                cleaning.separationEfficiency = Texts.getDouble(separationEfficiencyText);
				cleaning.description = descriptionText.getText();
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				else {
					setPageComplete(true);
					setErrorMessage(null);
					return true;
				}
			}

			boolean error(String message) {
				setErrorMessage(message);
				setPageComplete(false);
				return false;
			}
		}

	}
}