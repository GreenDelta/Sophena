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
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.SearchDialog;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class CostWizard extends Wizard {

	// private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private CostEditor editor;
	private ProductType type;
	private Product product;
	private ProductEntry productEntry;

	private Project project() {
		return editor.getProject();
	}

	public static int open(ProductEntry productEntry, ProductType type) {
		// ProductEntry en = new ProductEntry();
		if (productEntry == null)
			return Window.CANCEL;
		CostWizard w = new CostWizard();
		w.setWindowTitle(Labels.get(type));
		w.productEntry = productEntry;
		w.type = type;
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		// try {
		// page.data.bindToModel();
		// return true;
		// } catch (Exception e) {
		// log.error("failed to set product data " + product, e);
		return false;
	}
	// }

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
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

			costSection = new ProductCostSection(() -> productEntry.costs)
					.createFields(comp);
		}

		private void createProductRow(Composite comp) {
			UI.formLabel(comp, "Produkt");
			ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);

			if (product != null)
				link.setText(product.name);
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

		protected void selectProduct(ImageHyperlink link) {
			ProductDao dao = new ProductDao(App.getDb());
			Product p = SearchDialog.open((Labels.get(type) + " auswählen"),
					dao.getAll(type));
			if (p == null)
				return;
			product = p;
			link.setText(product.name);
			link.pack();

		}

	}

}
