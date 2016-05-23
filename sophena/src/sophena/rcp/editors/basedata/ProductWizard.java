package sophena.rcp.editors.basedata;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.db.daos.Dao;
import sophena.db.daos.ProductGroupDao;
import sophena.model.AbstractProduct;
import sophena.model.Manufacturer;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class ProductWizard extends Wizard {

	private Page page;
	private AbstractProduct product;
	private IContent content;

	public ProductWizard(AbstractProduct product, IContent content) {
		this.product = product;
		this.content = content;
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

	public void validate() {
		page.data.validate();
	}

	private class Page extends WizardPage {

		DataBinding data = new DataBinding();

		Text nameText;
		EntityCombo<ProductGroup> groupCombo;
		private EntityCombo<Manufacturer> manufacturerCombo;
		Text urlText;
		Text priceText;

		Text descriptionText;

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
			createManufacturerCombo(c);

			urlText = UI.formText(c, "Web-Link");
			Texts.on(urlText).required();
			UI.formLabel(c, "");

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR");

			content.render(c);

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

		private void createManufacturerCombo(Composite c) {
			manufacturerCombo = new EntityCombo<>();
			manufacturerCombo.create("Hersteller", c);
			Dao<Manufacturer> dao = new Dao<>(Manufacturer.class, App.getDb());
			List<Manufacturer> list = dao.getAll();
			Sorters.byName(list);
			manufacturerCombo.setInput(list);
			UI.filler(c);
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, product.name);
				groupCombo.select(product.group);
				manufacturerCombo.select(product.manufacturer);
				Texts.set(urlText, product.url);
				Texts.set(priceText, product.purchasePrice);
				Texts.set(descriptionText, product.description);
				content.bindToUI();
			}

			void bindToModel() {
				product.name = nameText.getText();
				product.group = groupCombo.getSelected();
				product.manufacturer = manufacturerCombo.getSelected();
				product.url = urlText.getText();
				product.description = descriptionText.getText();
				if (Texts.hasNumber(priceText))
					product.purchasePrice = Texts.getDouble(priceText);
				content.bindToModel();
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				String message = content.validate();
				if (message != null)
					return error(message);
				else {
					setPageComplete(!product.isProtected);
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

	/** Interface for product specific content in wizards. */
	public interface IContent {

		/** Render the components in a 3-column grid layout. */
		void render(Composite c);

		/** Bind the specific content to UI components. */
		void bindToUI();

		/** Bind the data of the UI components to the model. */
		void bindToModel();

		/**
		 * Validate the specific content: returns null if everything is fine,
		 * otherwise it returns the error message.
		 */
		String validate();
	}
}
