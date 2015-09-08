package sophena.rcp.editors.basedata.products;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Product;
import sophena.rcp.Labels;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class ProductWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Product product;

	public static int open(Product product) {
		if (product == null)
			return Window.CANCEL;
		ProductWizard wiz = new ProductWizard();
		wiz.setWindowTitle(Labels.get(product.type));
		wiz.product = product;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			page.data.bindToModel();
			return true;
		} catch (Exception e) {
			log.error("failed to set product data " + product, e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private DataBinding data = new DataBinding();

		private Text nameText;
		private Text linkText;
		private Text priceText;

		private Page() {
			super("FuelWizardPage", Labels.get(product.type), null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createNameText(composite);
			createLinkAndPriceText(composite);
			data.bindToUI();
		}

		private void createNameText(Composite composite) {
			nameText = UI.formText(composite, "Bezeichnung");
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(composite, "");

		}

		private void createLinkAndPriceText(Composite composite) {
			linkText = UI.formText(composite, "Web-Link");
			UI.formLabel(composite, "");
			priceText = UI.formText(composite, "Preis");
			Texts.on(priceText).decimal();
			UI.formLabel(composite, "EUR");
		}

		private class DataBinding {

			private void bindToModel() {

				product.name = nameText.getText();
				product.url = linkText.getText();
				if (Texts.hasNumber(priceText))
					product.purchasePrice = Texts.getDouble(priceText);
				else
					product.purchasePrice = null;
			}

			private void bindToUI() {
				Texts.set(nameText, product.name);
				Texts.set(linkText, product.url);
				Texts.set(priceText, product.purchasePrice);
				validate();
			}

			private boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");

				else {
					setPageComplete(true);
					setErrorMessage(null);
					return true;
				}
			}

			private boolean error(String message) {
				setErrorMessage(message);
				setPageComplete(false);
				return false;
			}

		}
	}
}
