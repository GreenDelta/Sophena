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
import sophena.model.HeatRecovery;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class HeatRecoveryWizard extends Wizard {

	private HeatRecovery recovery;
	private Page page;

	static int open(HeatRecovery recovery) {
		if (recovery == null)
			return Window.CANCEL;
		HeatRecoveryWizard w = new HeatRecoveryWizard();
		w.setWindowTitle("Wärmerückgewinnung");
		w.recovery = recovery;
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(180, 410);
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

		Text fuelText;
		Text powerText;
		Text typeText;
		Text producerPowerText;
		Text descriptionText;

		Page() {
			super("HeatRecoveryPage", "Wärmerückgewinnung", null);
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
			Texts.on(manufacturerText).required();
			UI.filler(c);

			urlText = UI.formText(c, "Web-Link");
			Texts.on(urlText).required().validate(data::validate);
			UI.filler(c);

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR");

			powerText = UI.formText(c, "Thermische Leistung");
			Texts.on(powerText).decimal().required();
			UI.formLabel(c, "kW");

			typeText = UI.formText(c, "Art des Wärmeerzeugers");
			Texts.on(typeText).required();
			UI.filler(c);

			fuelText = UI.formText(c, "Brennstoff (Wärmeerzeuger)");
			Texts.on(fuelText).required();
			UI.filler(c);

			producerPowerText = UI.formText(c, "Leistung des Wärmeerzeugers");
			Texts.on(producerPowerText).decimal().required();
			UI.formLabel(c, "kW");

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.filler(c);

			data.bindToUI();
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(ProductType.HEAT_RECOVERY);
			Sorters.byName(list);
			groupCombo.setInput(list);
			UI.formLabel(c, "");
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, recovery.name);
				groupCombo.select(recovery.group);
				Texts.set(urlText, recovery.url);
				Texts.set(priceText, recovery.purchasePrice);
				Texts.set(powerText, recovery.power);
				Texts.set(typeText, recovery.heatRecoveryType);
				Texts.set(producerPowerText, recovery.producerPower);
				Texts.set(descriptionText, recovery.description);
				Texts.set(fuelText, recovery.fuel);
			}

			void bindToModel() {
				recovery.name = nameText.getText();
				recovery.group = groupCombo.getSelected();
				recovery.url = urlText.getText();
				if (Texts.hasNumber(priceText))
					recovery.purchasePrice = Texts.getDouble(priceText);
				recovery.power = Texts.getDouble(powerText);
				recovery.heatRecoveryType = typeText.getText();
				recovery.producerPower = Texts.getDouble(producerPowerText);
				recovery.description = descriptionText.getText();
				recovery.fuel = fuelText.getText();
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				else {
					setPageComplete(!recovery.isProtected);
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