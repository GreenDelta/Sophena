package sophena.rcp.wizards;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.FlueGasCleaning;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.ProductCosts;
import sophena.rcp.Icon;
import sophena.rcp.SearchDialog;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

public class FlueGasCleaningEntryWizard extends Wizard {

	private FlueGasCleaningEntry entry;

	private Page page;

	public static int open(FlueGasCleaningEntry entry) {
		if (entry == null)
			return Window.CANCEL;
		FlueGasCleaningEntryWizard w = new FlueGasCleaningEntryWizard();
		w.entry = entry;
		w.setWindowTitle("Rauchgasreinigung");
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		if (page.selectedProduct == null) {
			noProduct(true);
			return false;
		}
		entry.costs = page.costs;
		entry.product = page.selectedProduct;
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
		private FlueGasCleaning selectedProduct;
		private ProductCosts costs;

		Page() {
			super("OverviewPage", "Rauchgasreinigung", null);
			selectedProduct = entry.product;
			costs = entry.costs != null
					? entry.costs.clone()
					: new ProductCosts();
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createProductLink(comp);
			costSection = new ProductCostSection(() -> costs)
					.createFields(comp);
		}

		private void createProductLink(Composite comp) {
			UI.formLabel(comp, "Produkt");
			ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);
			if (selectedProduct != null) {
				link.setText(selectedProduct.name);
			} else {
				link.setText("(kein Produkt ausgewählt)");
			}
			link.setImage(Icon.FLUE_GAS_16.img());
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
			FlueGasCleaning c = SearchDialog.open("Rauchgasreinigung",
					FlueGasCleaning.class);
			if (c == null)
				return;
			selectedProduct = c;
			link.setText(c.name);
			ProductCosts.copy(c.group, costs);
			costSection.refresh();
		}
	}
}
