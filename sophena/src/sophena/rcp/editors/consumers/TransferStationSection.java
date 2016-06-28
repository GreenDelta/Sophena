package sophena.rcp.editors.consumers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.Consumer;
import sophena.model.ProductCosts;
import sophena.model.TransferStation;
import sophena.rcp.Icon;
import sophena.rcp.SearchDialog;
import sophena.rcp.SearchLabel;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.DeleteLink;
import sophena.rcp.utils.UI;

class TransferStationSection {

	private ConsumerEditor editor;
	private ProductCostSection costSection;

	TransferStationSection(ConsumerEditor editor) {
		this.editor = editor;
	}

	private Consumer consumer() {
		return editor.consumer;
	}

	void create(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "Hausübergabestation");
		UI.gridLayout(comp, 3);
		createProductRow(comp, tk);
		if (consumer().transferStationCosts == null) {
			consumer().transferStationCosts = new ProductCosts();
		}
		costSection = new ProductCostSection(() -> consumer().transferStationCosts)
				.withEditor(editor)
				.createFields(comp, tk);
	}

	private void createProductRow(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, "Produkt");
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 2);
		ImageHyperlink link = new ImageHyperlink(inner, SWT.TOP);
		if (consumer().transferStation != null) {
			link.setText(consumer().transferStation.name);
		} else {
			link.setText("(keine Hausübergabestation ausgewählt)");
		}
		link.setImage(Icon.CONSUMER_16.img());
		link.setForeground(Colors.getLinkBlue());
		Controls.onClick(link, e -> selectTransferStation(link));
		createDeleteLink(inner, link);
		UI.formLabel(comp, tk, "");
	}

	private void createDeleteLink(Composite comp, ImageHyperlink link) {
		DeleteLink.on(comp, () -> {
			if (consumer().transferStation == null)
				return;
			consumer().transferStation = null;
			link.setText("(keine Hausübergabestation ausgewählt)");
			link.getParent().pack();
			editor.setDirty();
		});
	}

	private void selectTransferStation(ImageHyperlink link) {
		TransferStation s = SearchDialog.open("Hausübergabestation",
				TransferStation.class, SearchLabel::forTransferStation);
		if (s == null)
			return;
		consumer().transferStation = s;
		link.setText(s.name);
		link.getParent().pack();
		ProductCosts costs = consumer().transferStationCosts;
		ProductCosts.copy(s.group, costs);
		if (s.purchasePrice != null)
			costs.investment = s.purchasePrice;
		costSection.refresh();
	}
}
