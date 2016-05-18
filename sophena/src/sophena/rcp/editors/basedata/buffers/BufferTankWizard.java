package sophena.rcp.editors.basedata.buffers;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.db.daos.ProductGroupDao;
import sophena.model.BufferTank;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BufferTankWizard extends Wizard {

	private Page page;
	private BufferTank buffer;

	static int open(BufferTank buffer) {
		if (buffer == null)
			return Window.CANCEL;
		BufferTankWizard w = new BufferTankWizard();
		w.setWindowTitle("Pufferspeicher");
		w.buffer = buffer;
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(150, 410);
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
		Text volText;
		Text diameterText;
		Text heightText;
		Text insulationText;
		Text descriptionText;

		Page() {
			super("BufferWizardPage", "Pufferspeicher", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			createGroupCombo(c);

			nameText = UI.formText(c, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");

			manufacturerText = UI.formText(c, "Hersteller");
			Texts.on(manufacturerText).required().validate(data::validate);
			UI.formLabel(c, "");

			urlText = UI.formText(c, "Web-Link");
			Texts.on(urlText).required().validate(data::validate);
			UI.formLabel(c, "");

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR");

			volText = UI.formText(c, "Volumen");
			Texts.on(volText).required().decimal().validate(data::validate);
			UI.formLabel(c, "L");

			diameterText = UI.formText(c, "Durchmesser");
			UI.formLabel(c, "mm");

			heightText = UI.formText(c, "Höhe");
			UI.formLabel(c, "mm");

			insulationText = UI.formText(c, "Isolierung");
			UI.formLabel(c, "mm");

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.formLabel(c, "");

			data.bindToUI();
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(ProductType.BUFFER_TANK);
			Sorters.byName(list);
			groupCombo.setInput(list);
			UI.formLabel(c, "");
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, buffer.name);
				groupCombo.select(buffer.group);
				// Texts.set(manufacturerText, buffer.manufacturer.name);
				Texts.set(volText, buffer.volume);
				Texts.set(urlText, buffer.url);
				Texts.set(priceText, buffer.purchasePrice);
				Texts.set(diameterText, buffer.diameter);
				Texts.set(heightText, buffer.height);
				Texts.set(insulationText, buffer.insulationThickness);
				Texts.set(descriptionText, buffer.description);
			}

			void bindToModel() {
				buffer.name = nameText.getText();
				buffer.group = groupCombo.getSelected();
				// buffer.manufacturer.name = manufacturerText.getText();
				buffer.volume = Texts.getDouble(volText);
				buffer.diameter = Texts.getDouble(diameterText);
				buffer.height = Texts.getDouble(heightText);
				buffer.insulationThickness = Texts.getDouble(insulationText);
				buffer.url = urlText.getText();
				buffer.description = descriptionText.getText();
				if (Texts.hasNumber(priceText))
					buffer.purchasePrice = Texts.getDouble(priceText);
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				if (!Texts.hasNumber(volText))
					return error("Es muss ein Volumen angegeben werden.");
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
