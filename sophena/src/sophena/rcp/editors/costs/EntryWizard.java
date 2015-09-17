package sophena.rcp.editors.costs;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.db.daos.ProductDao;
import sophena.model.Product;
import sophena.model.ProductEntry;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.SearchDialog;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class EntryWizard extends Wizard {

	private Page page;
	private ProductType type;
	private ProductEntry entry;

	public static int open(ProductEntry entry, ProductType type) {
		if (entry == null)
			return Window.CANCEL;
		EntryWizard w = new EntryWizard();
		w.setWindowTitle(Labels.get(type));
		w.entry = entry;
		w.type = type;
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
		if (b)
			page.setErrorMessage("Es wurde kein Produkt ausgewählt");
		else
			page.setErrorMessage(null);
	}

	private class Page extends WizardPage {

		private ProductCostSection costSection;

		Page() {
			super("OverviewPage", Labels.get(type), null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createProductRow(comp);
			costSection = new ProductCostSection(() -> entry.costs)
					.createFields(comp);
		}

		private void createProductRow(Composite comp) {
			UI.formLabel(comp, "Produkt");
			ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);
			if (entry.product != null)
				link.setText(entry.product.name);
			else
				link.setText("(kein Produkt ausgewählt)");
			link.setImage(Images.PIPE_16.img());
			link.setForeground(Colors.getLinkBlue());
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					selectProduct(link);
				}
			});
			UI.formLabel(comp, "");
		}

		private void selectProduct(ImageHyperlink link) {
			ProductDao dao = new ProductDao(App.getDb());
			Product p = SearchDialog.open((Labels.get(type)), dao.getAll(type));
			if (p == null) {
				noProduct(true);
				return;
			}
			noProduct(false);
			entry.product = p;
			if (p.purchasePrice != null) {
				entry.costs.investment = p.purchasePrice;
				costSection.refresh();
			}
			link.setText(p.name);
			link.pack();
		}
	}
}
