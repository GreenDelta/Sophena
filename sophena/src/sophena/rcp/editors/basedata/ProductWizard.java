package sophena.rcp.editors.basedata;

import java.util.List;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.db.daos.Dao;
import sophena.db.daos.ProductGroupDao;
import sophena.model.AbstractProduct;
import sophena.model.Manufacturer;
import sophena.model.Pipe;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Desktop;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

public class ProductWizard extends Wizard {

	private Page page;
	private final AbstractProduct product;
	private final IContent content;

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
			super("WizardPage", content.getPageName(), null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
			sc.setExpandVertical(true);
			sc.setExpandHorizontal(true);
			Composite cParent = new Composite(sc, SWT.NULL);
			UI.gridLayout(cParent, 1);
			sc.setContent(cParent);
			setControl(cParent);
			Composite c = new Composite(cParent, SWT.NULL);
			UI.gridData(c, true, false);
			UI.gridLayout(c, 3);
			nameText = UI.formText(c, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");

			createGroupCombo(c);
			createManufacturerCombo(c);
			createWebLink(c);
			createPriceText(c);

			content.render(c);
			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.formLabel(c, "");
			data.bindToUI();

			sc.setMinSize(cParent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}

		private void createPriceText(Composite c) {
			priceText = UI.formText(c, "Preis");
			if (product instanceof Pipe) {
				UI.formLabel(c, "EUR/m");
			} else {
				UI.formLabel(c, "EUR");
			}
		}

		private void createWebLink(Composite c) {
			if (!product.isProtected) {
				urlText = UI.formText(c, "Web-Link");
				Texts.on(urlText).required();
				ImageHyperlink link = new ImageHyperlink(c, SWT.NONE);
				link.setImage(Icon.WEBLINK_16.img());
				Controls.onClick(link,
						e -> Desktop.browse(urlText.getText()));
				return;
			}
			UI.formLabel(c, "Web-Link");
			Hyperlink link = new Hyperlink(c, SWT.NONE);
			link.setForeground(Colors.getLinkBlue());
			if (product.url == null) {
				link.setText(""); // SWT throws a NullPointer otherwise
			} else {
				link.setText(Strings.cut(product.url, 60));
				link.setToolTipText(product.url);
			}
			UI.filler(c);
			Controls.onClick(link, e -> {
				Desktop.browse(product.url);
			});
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(content.getProductType());
			Sorters.productGroups(list);
			groupCombo.setInput(list);
			if (!list.isEmpty())
				groupCombo.select(list.get(0));
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
				product.description = descriptionText.getText();
				if (urlText != null) {
					product.url = urlText.getText();
				}
				if (Texts.hasNumber(priceText)) {
					product.purchasePrice = Texts.getDouble(priceText);
				}
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

		String getPageName();

		ProductType getProductType();

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
