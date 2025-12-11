package sophena.rcp.editors.basedata;

import java.util.List;
import java.util.Objects;

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
		Text productLineText;
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
			var sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);

			sc.setExpandVertical(true);
			sc.setExpandHorizontal(true);
			var cParent = new Composite(sc, SWT.NULL);
			UI.gridLayout(cParent, 1);
			sc.setContent(cParent);
			setControl(cParent);

			var comp = new Composite(cParent, SWT.NULL);
			UI.gridData(comp, true, false);
			UI.gridLayout(comp, 3);
			nameText = Texts.on(UI.formText(comp, M.Name))
				.disableWhen(product.isProtected)
				.required()
				.validate(data::validate)
				.get();
			UI.formLabel(comp, "");

			productLineText = Texts.on(UI.formText(comp, "Produktlinie"))
				.disableWhen(product.isProtected)
				.get();
			UI.formLabel(comp, "");

			createGroupCombo(comp);
			createManufacturerCombo(comp);
			createWebLink(comp);
			createPriceText(comp);

			content.render(comp);
			descriptionText = Texts.on(UI.formMultiText(comp, "Zusatzinformation"))
				.disableWhen(product.isProtected)
				.get();
			UI.formLabel(comp, "");
			data.bindToUI();

			sc.setMinSize(cParent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		}

		private void createPriceText(Composite c) {
			priceText = Texts.on(UI.formText(c, "Preis"))
				.decimal()
				.validate(data::validate)
				.get();
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
				var link = new ImageHyperlink(c, SWT.NONE);
				link.setImage(Icon.WEBLINK_16.img());
				Controls.onClick(link,
						e -> Desktop.browse(urlText.getText()));
				return;
			}

			UI.formLabel(c, "Web-Link");
			var link = new Hyperlink(c, SWT.NONE);
			link.setForeground(Colors.getLinkBlue());
			if (product.url == null) {
				link.setText(""); // SWT throws a NullPointer otherwise
			} else {
				link.setText(Strings.cut(product.url, 60));
				link.setToolTipText(product.url);
			}
			UI.filler(c);
			Controls.onClick(link, e -> Desktop.browse(product.url));
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			groupCombo.getControl().setEnabled(!product.isProtected);
			var dao = new ProductGroupDao(App.getDb());
			var list = dao.getAll(content.getProductType());
			Sorters.productGroups(list);
			groupCombo.setInput(list);
			if (!list.isEmpty())
				groupCombo.select(list.getFirst());
			UI.formLabel(c, "");
		}

		private void createManufacturerCombo(Composite c) {
			manufacturerCombo = new EntityCombo<>();
			manufacturerCombo.create("Hersteller", c);
			manufacturerCombo.getControl().setEnabled(!product.isProtected);
			Dao<Manufacturer> dao = new Dao<>(Manufacturer.class, App.getDb());
			List<Manufacturer> list = dao.getAll();
			Sorters.byName(list);
			manufacturerCombo.setInput(list);
			UI.filler(c);
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, product.name);
				Texts.set(productLineText, product.productLine);
				groupCombo.select(product.group);
				manufacturerCombo.select(product.manufacturer);
				Texts.set(urlText, product.url);
				Texts.set(priceText, product.purchasePrice);
				Texts.set(descriptionText, product.description);
				content.bindToUI();
			}

			void bindToModel() {
				if (product.isProtected) {
					if (Texts.hasNumber(priceText)) {
						product.purchasePrice = Texts.getDouble(priceText);
					} else {
						product.purchasePrice = null;
					}
					return;
				}
				product.name = nameText.getText();
				product.productLine = productLineText.getText();
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
				if (product.isProtected) {
					boolean changed = hasPriceChanged();
					setPageComplete(changed);
					setErrorMessage(null);
					return changed;
				}

				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				var message = content.validate();
				if (message != null)
					return error(message);
				setPageComplete(true);
				setErrorMessage(null);
				return true;
			}

			private boolean hasPriceChanged() {
				Double next = Texts.hasNumber(priceText)
						? Texts.getDouble(priceText)
						: null;
				var origin = product.purchasePrice;
				return !Objects.equals(next, origin);
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
