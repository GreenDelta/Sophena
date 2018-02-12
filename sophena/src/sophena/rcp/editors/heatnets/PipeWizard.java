package sophena.rcp.editors.heatnets;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.HeatNetPipe;
import sophena.model.Pipe;
import sophena.model.ProductCosts;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.SearchDialog;
import sophena.rcp.SearchLabel;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class PipeWizard extends Wizard {

	private Page page;
	private HeatNetPipe pipe;

	static int open(HeatNetPipe pipe) {
		if (pipe == null)
			return Window.CANCEL;
		PipeWizard w = new PipeWizard();
		w.setWindowTitle("Wärmeleitung");
		w.pipe = pipe;
		if (pipe.costs == null)
			pipe.costs = new ProductCosts();
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		if (pipe.pipe == null)
			return false;
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private ProductCostSection costSection;
		private Text priceText;

		Page() {
			super("HeatNetPipePage", "Wärmeleitung", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createProductRow(comp);
			createNameText(comp);
			createLengthText(comp);
			createPriceText(comp);
			costSection = new ProductCostSection(() -> pipe.costs)
					.createFields(comp);
			Texts.on(costSection.investmentText).calculated();
		}

		private void createNameText(Composite comp) {
			Text t = UI.formText(comp, M.Name);
			Texts.on(t).init(pipe.name).onChanged((s) -> {
				pipe.name = s;
			});
			UI.filler(comp);
		}

		private void createProductRow(Composite comp) {
			UI.formLabel(comp, "Produkt");
			ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);
			if (pipe.pipe != null)
				link.setText(pipe.pipe.name);
			else
				link.setText("(keine Wärmeleitung ausgewählt)");
			link.setImage(Icon.PIPE_16.img());
			link.setForeground(Colors.getLinkBlue());
			Controls.onClick(link, e -> selectPipe(link));
			UI.formLabel(comp, "");

		}

		private void selectPipe(ImageHyperlink link) {
			Pipe p = SearchDialog.open("Wärmeleitung", Pipe.class,
					SearchLabel::forPipe);
			if (p == null)
				return;
			pipe.pipe = p;
			link.setText(p.name);
			link.pack();
			double price = p.purchasePrice == null ? 0 : p.purchasePrice;
			pipe.pricePerMeter = price;
			Texts.set(priceText, price);
			ProductCosts.copy(p.group, pipe.costs);
			updateCosts();
		}

		private void createLengthText(Composite comp) {
			Text t = UI.formText(comp, "Länge");
			Texts.on(t).init(pipe.length).decimal().required()
					.onChanged(s -> {
						pipe.length = Texts.getDouble(t);
						updateCosts();
					});
			UI.formLabel(comp, "m");
		}

		private void createPriceText(Composite comp) {
			priceText = UI.formText(comp, "Preis");
			Texts.on(priceText).init(pipe.pricePerMeter).decimal().required()
					.onChanged(s -> {
						pipe.pricePerMeter = Texts.getDouble(priceText);
						updateCosts();
					});
			UI.formLabel(comp, "EUR/m");
		}

		private void updateCosts() {
			pipe.costs.investment = pipe.length * pipe.pricePerMeter;
			costSection.refresh();
		}

	}
}
