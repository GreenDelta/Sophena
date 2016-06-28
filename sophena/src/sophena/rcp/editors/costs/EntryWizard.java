package sophena.rcp.editors.costs;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.db.daos.ProductDao;
import sophena.db.daos.ProductGroupDao;
import sophena.model.Product;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.SearchDialog;
import sophena.rcp.SearchLabel;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

/**
 * Wizard for product entries in the cost page. If the product of the respective
 * product entry has a project ID it means that the product is project-private.
 * In this case also the product attributes (name etc.) can be edited via this
 * wizard.
 */
class EntryWizard extends Wizard {

	private Page page;
	private ProductType type;
	private ProductEntry entry;
	private int projectDuration;

	public static int open(ProductEntry entry, ProductType type,
			int projectDuration) {
		if (entry == null)
			return Window.CANCEL;
		EntryWizard w = new EntryWizard();
		w.setWindowTitle(Labels.get(type));
		w.entry = entry;
		w.type = type;
		w.projectDuration = projectDuration;
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		if (entry.product == null) {
			noProduct(true);
			return false;
		}
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private void noProduct(boolean b) {
		if (b) {
			page.setErrorMessage("Es wurde kein Produkt ausgewählt");
		} else {
			page.setErrorMessage(null);
		}
	}

	private class Page extends WizardPage {

		private ProductCostSection costSection;
		private Text priceText;

		Page() {
			super("OverviewPage", Labels.get(type), null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			if (entry.product == null || entry.product.projectId == null)
				createGlobalProductLink(comp);
			else {
				privateNameText(comp);
				privateGroupCombo(comp);
			}
			createPriceText(comp);
			createCountText(comp);
			costSection = new ProductCostSection(() -> entry.costs)
					.createFields(comp);
			Texts.on(costSection.investmentText).calculated();
		}

		private void createGlobalProductLink(Composite comp) {
			UI.formLabel(comp, "Produkt");
			ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);
			if (entry.product != null)
				link.setText(entry.product.name);
			else
				link.setText("(kein Produkt ausgewählt)");
			link.setImage(Icon.PIPE_16.img());
			link.setForeground(Colors.getLinkBlue());
			Controls.onClick(link, e -> searchGlobalProduct(link));
			UI.formLabel(comp, "");
		}

		private void searchGlobalProduct(ImageHyperlink link) {
			ProductDao dao = new ProductDao(App.getDb());
			Product p = SearchDialog.open(Labels.get(type),
					dao.getAllGlobal(type), SearchLabel::forProduct);
			if (p == null) {
				noProduct(true);
				return;
			}
			noProduct(false);
			entry.product = p;
			link.setText(p.name);
			link.pack();
			double price = p.purchasePrice == null ? 0 : p.purchasePrice;
			entry.pricePerPiece = price;
			Texts.set(priceText, price);
			ProductCosts.copy(p.group, entry.costs);
			if (entry.costs.duration == 0) {
				entry.costs.duration = projectDuration;
			}
			updateCosts();
		}

		private void privateNameText(Composite comp) {
			Text t = UI.formText(comp, "Bezeichnung");
			if (entry.product != null)
				Texts.set(t, entry.product.name);
			Texts.on(t).required().onChanged(s -> {
				if (entry.product != null) {
					entry.product.name = s;
				}
			});
			UI.filler(comp);
		}

		private void privateGroupCombo(Composite comp) {
			EntityCombo<ProductGroup> combo = new EntityCombo<>();
			combo.create("Produktgruppe", comp);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(type);
			Sorters.byName(list);
			combo.setInput(list);
			if (entry.product != null && entry.product.group != null)
				combo.select(entry.product.group);
			combo.onSelect(group -> {
				if (entry.product == null) {
					return;
				}
				entry.product.group = group;
				ProductCosts.copy(group, entry.costs);
				if (entry.costs.duration == 0) {
					entry.costs.duration = projectDuration;
				}
				costSection.refresh();
			});
			UI.filler(comp);
		}

		private void createCountText(Composite comp) {
			Text t = UI.formText(comp, "Anzahl");
			Texts.on(t).init(entry.count).decimal().required().onChanged(s -> {
				entry.count = Texts.getDouble(t);
				updateCosts();
			});
			UI.formLabel(comp, "Stück");
		}

		private void createPriceText(Composite comp) {
			priceText = UI.formText(comp, "Preis pro Stück");
			Texts.on(priceText).init(entry.pricePerPiece).decimal().required()
					.onChanged(s -> {
						entry.pricePerPiece = Texts.getDouble(priceText);
						updateCosts();
					});
			UI.formLabel(comp, "EUR/Stück");
		}

		private void updateCosts() {
			entry.costs.investment = entry.count * entry.pricePerPiece;
			costSection.refresh();
		}
	}
}
