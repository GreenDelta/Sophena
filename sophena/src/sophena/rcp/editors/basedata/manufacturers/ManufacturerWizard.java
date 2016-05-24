package sophena.rcp.editors.basedata.manufacturers;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.Manufacturer;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class ManufacturerWizard extends Wizard {

	private Page page;
	private Manufacturer manufacturer;

	static int open(Manufacturer manufacturer) {
		if (manufacturer == null)
			return Window.CANCEL;
		ManufacturerWizard w = new ManufacturerWizard();
		w.setWindowTitle("Hersteller");
		w.manufacturer = manufacturer;
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(150, 330);
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
		Text addressText;
		Text urlText;
		Text descriptionText;

		Page() {
			super("ManufacturerWizardPage", "Hersteller", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			nameText = UI.formText(c, "Name");
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");

			addressText = UI.formMultiText(c, "Adresse");
			UI.formLabel(c, "");

			urlText = UI.formText(c, "Web-Link");
			UI.formLabel(c, "");

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.filler(c);

			data.bindToUI();
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, manufacturer.name);
				Texts.set(addressText, manufacturer.address);
				Texts.set(urlText, manufacturer.url);
				Texts.set(descriptionText, manufacturer.description);

			}

			void bindToModel() {
				manufacturer.name = nameText.getText();
				manufacturer.address = addressText.getText();
				manufacturer.description = descriptionText.getText();
				manufacturer.url = urlText.getText();

			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				else {
					setPageComplete(!manufacturer.isProtected);
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