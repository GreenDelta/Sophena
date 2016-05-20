package sophena.rcp.editors.basedata.transfer.stations;

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

public class TransferStationWizard extends Wizard {

	private TransferStation station;
	private Page page;

	static int open(TransferStation station) {
		if (station == null)
			return Window.CANCEL;
		TransferStationWizard w = new TransferStationWizard();
		w.setWindowTitle("Hausübergabestation");
		w.station = station;
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(180, 600);
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

		EntityCombo<ProductGroup> groupCombo;
		Text nameText;
		Text manufacturerText;
		Text urlText;
		Text priceText;

		private Text buildingTypeText;
		private Text capacityText;
		private Text typeText;
		private Text materialText;
		private Text waterHeatingText;
		private Text controlText;
		private Text descriptionText;

		Page() {
			super("TransferStationWizardPage", "Hausübergabestation", null);
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

			buildingTypeText = UI.formText(c, M.BuildingType);
			Texts.on(buildingTypeText).required();
			UI.filler(c);

			capacityText = UI.formText(c, "Leistung");
			Texts.on(capacityText).required();
			UI.formLabel(c, "kW");

			typeText = UI.formMultiText(c, "Art");
			Texts.on(typeText).required();
			UI.filler(c);

			materialText = UI.formMultiText(c, "Material");
			Texts.on(materialText).required();
			UI.filler(c);

			waterHeatingText = UI.formMultiText(c, "Warmwasserbereitung");
			UI.filler(c);

			controlText = UI.formMultiText(c, "Regelung");
			UI.filler(c);

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.filler(c);

			data.bindToUI();
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(ProductType.TRANSFER_STATION);
			Sorters.byName(list);
			groupCombo.setInput(list);
			UI.formLabel(c, "");
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, station.name);
				groupCombo.select(station.group);
				Texts.set(urlText, station.url);
				Texts.set(priceText, station.purchasePrice);
				Texts.set(buildingTypeText, station.buildingType);
				Texts.set(capacityText, station.outputCapacity);
				Texts.set(typeText, station.stationType);
				Texts.set(materialText, station.material);
				Texts.set(waterHeatingText, station.waterHeating);
				Texts.set(controlText, station.control);
				Texts.set(descriptionText, station.description);
			}

			void bindToModel() {
				station.name = nameText.getText();
				station.group = groupCombo.getSelected();
				station.url = urlText.getText();
				if (Texts.hasNumber(priceText))
					station.purchasePrice = Texts.getDouble(priceText);
				station.buildingType = buildingTypeText.getText();
				station.outputCapacity = Texts.getDouble(capacityText);
				station.stationType = typeText.getText();
				station.material = materialText.getText();
				station.waterHeating = waterHeatingText.getText();
				station.control = controlText.getText();
				station.description = descriptionText.getText();
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				else {
					setPageComplete(!station.isProtected);
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
