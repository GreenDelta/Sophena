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
		d.setPageSize(150, 400);
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
		Text volText;
		Text urlText;
		Text priceText;

		Page() {
			super("BufferWizardPage", "Pufferspeicher", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			nameText = UI.formText(c, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");

			createGroupCombo(c);

			volText = UI.formText(c, "Volumen");
			Texts.on(volText).required().decimal().validate(data::validate);
			UI.formLabel(c, "L");

			urlText = UI.formText(c, "Web-Link");
			UI.formLabel(c, "");

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR");

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
				Texts.set(volText, buffer.volume);
				Texts.set(urlText, buffer.url);
				Texts.set(priceText, buffer.purchasePrice);
			}

			void bindToModel() {
				buffer.name = nameText.getText();
				buffer.group = groupCombo.getSelected();
				buffer.volume = Texts.getDouble(volText);
				buffer.url = urlText.getText();
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
