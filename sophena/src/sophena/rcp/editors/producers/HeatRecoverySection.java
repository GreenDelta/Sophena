package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.HeatRecovery;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.rcp.Icon;
import sophena.rcp.SearchDialog;
import sophena.rcp.SearchLabel;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.DeleteLink;
import sophena.rcp.utils.UI;

class HeatRecoverySection {

	private ProducerEditor editor;
	private ProductCostSection costSection;

	HeatRecoverySection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	void create(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "Wärmerückgewinnung");
		UI.gridLayout(comp, 3);
		createProductRow(comp, tk);
		if (producer().heatRecoveryCosts == null) {
			producer().heatRecoveryCosts = new ProductCosts();
		}
		costSection = new ProductCostSection(() -> producer().heatRecoveryCosts)
				.withEditor(editor)
				.createFields(comp, tk);
	}

	private void createProductRow(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, "Produkt");
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 2);
		ImageHyperlink link = new ImageHyperlink(inner, SWT.TOP);
		if (producer().heatRecovery != null) {
			link.setText(producer().heatRecovery.name);
		} else {
			link.setText("(keine Wärmerückgewinnung ausgewählt)");
		}
		link.setImage(Icon.HEAT_RECOVERY_16.img());
		link.setForeground(Colors.getLinkBlue());
		Controls.onClick(link, e -> selectHeatRecovery(link));
		createDeleteLink(inner, link);
		UI.formLabel(comp, tk, "");
	}

	private void createDeleteLink(Composite comp, ImageHyperlink link) {
		DeleteLink.on(comp, () -> {
			if (producer().heatRecovery == null)
				return;
			producer().heatRecovery = null;
			link.setText("(keine Wärmerückgewinnung ausgewählt)");
			link.getParent().pack();
			editor.setDirty();
		});
	}

	private void selectHeatRecovery(ImageHyperlink link) {
		HeatRecovery hr = SearchDialog.open("Wärmerückgewinnung",
				HeatRecovery.class, SearchLabel::forHeatRecovery);
		if (hr == null)
			return;
		producer().heatRecovery = hr;
		link.setText(hr.name);
		link.getParent().pack();
		ProductCosts costs = producer().heatRecoveryCosts;
		ProductCosts.copy(hr, costs);
		costSection.refresh();
		editor.setDirty();
	}
}