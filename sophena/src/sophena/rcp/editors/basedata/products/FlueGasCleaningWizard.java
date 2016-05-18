package sophena.rcp.editors.basedata.products;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.db.daos.ProductGroupDao;
import sophena.model.FlueGasCleaning;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
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
		d.setPageSize(200, 520);
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
		Text manufacturerText;
		Text urlText;
		Text priceText;
		private Text maxVolumeFlowText;

		// TODO: fuel
		Combo fuelCombo;
		private Text maxProducerPowerText;
		private Text maxElectricityConsumptionText;
		private Text cleaningMethodText;
		private Text cleaningTypeText;
		private Text separationEfficiencyText;
		private Text descriptionText;

		Page() {
			super("FlueGasCleaningPage", "Rauchgasreinigung", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			createGroupCombo(c);

			nameText = UI.formText(c, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.filler(c);

			manufacturerText = UI.formText(c, "Hersteller");
			Texts.on(manufacturerText).required().validate(data::validate);
			UI.filler(c);

			urlText = UI.formText(c, "Web-Link");
			Texts.on(urlText).required().validate(data::validate);
			UI.filler(c);

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR");

			maxVolumeFlowText = UI.formText(c, "Max. reinigbarer Volumenstrom");
			Texts.on(maxVolumeFlowText).decimal().required();
			UI.formLabel(c, "m3/h");

			fuelCombo = UI.formCombo(c, M.Fuel);
			UI.formLabel(c, "");

			maxProducerPowerText = UI.formText(c, "Max. Leistung Wärmeerzeuger");
			Texts.on(maxProducerPowerText).decimal().required();
			UI.formLabel(c, "kW");

			maxElectricityConsumptionText = UI.formText(c, "Eigenstrombedarf");
			Texts.on(maxElectricityConsumptionText).decimal();
			UI.formLabel(c, "kW");

			cleaningMethodText = UI.formText(c, "Art der Reinigung");
			UI.filler(c);

			cleaningTypeText = UI.formText(c, "Typ der Reinigung");
			UI.filler(c);

			separationEfficiencyText = UI.formText(c, "Max. Abscheidegrad");
			Texts.on(separationEfficiencyText).decimal();
			UI.formLabel(c, "%");

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.filler(c);

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
				Texts.set(maxVolumeFlowText, cleaning.maxVolumeFlow);
				Texts.set(maxProducerPowerText, cleaning.maxProducerPower);
				Texts.set(maxElectricityConsumptionText, cleaning.maxElectricityConsumption);
				Texts.set(cleaningMethodText, cleaning.cleaningMethod);
				Texts.set(cleaningTypeText, cleaning.cleaningType);
				Texts.set(separationEfficiencyText, cleaning.separationEfficiency);
				Texts.set(descriptionText, cleaning.description);
			}

			void bindToModel() {
				cleaning.name = nameText.getText();
				cleaning.group = groupCombo.getSelected();
				cleaning.url = urlText.getText();
				if (Texts.hasNumber(priceText))
					cleaning.purchasePrice = Texts.getDouble(priceText);
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