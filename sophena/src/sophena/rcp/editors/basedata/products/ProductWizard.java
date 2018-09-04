package sophena.rcp.editors.basedata.products;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProductGroupDao;
import sophena.model.Product;
import sophena.model.ProductGroup;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Desktop;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
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
		private EntityCombo<ProductGroup> groupCombo;
		private Text linkText;
		private Text priceText;

		private Page() {
			super("FuelWizardPage", Labels.get(product.type), null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createNameAndGroup(composite);
			createLinkAndPrice(composite);
			data.bindToUI();
		}

		private void createNameAndGroup(Composite c) {
			nameText = UI.formText(c, "Bezeichnung");
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(product.type);
			Sorters.byName(list);
			groupCombo.setInput(list);
			if (!list.isEmpty())
				groupCombo.select(list.get(0));
			UI.formLabel(c, "");
		}

		private void createLinkAndPrice(Composite c) {
			linkText = UI.formText(c, "Web-Link");
			ImageHyperlink link = new ImageHyperlink(c, SWT.NONE);
			link.setImage(Icon.WEBLINK_16.img());
			Controls.onClick(link,
					e -> Desktop.browse(linkText.getText()));
			priceText = UI.formText(c, "Preis");
			Texts.on(priceText).decimal();
			UI.formLabel(c, "EUR");
		}

		private class DataBinding {

			private void bindToModel() {
				product.name = nameText.getText();
				product.group = groupCombo.getSelected();
				product.url = linkText.getText();
				if (Texts.hasNumber(priceText))
					product.purchasePrice = Texts.getDouble(priceText);
				else
					product.purchasePrice = null;
			}

			private void bindToUI() {
				Texts.set(nameText, product.name);
				groupCombo.select(product.group);
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
