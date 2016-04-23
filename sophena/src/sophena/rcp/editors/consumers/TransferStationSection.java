package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.Consumer;
import sophena.model.ProductCosts;
import sophena.rcp.editors.ProductCostSection;
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
		if (consumer().transferStationCosts == null) {
			consumer().transferStationCosts = new ProductCosts();
		}
		costSection = new ProductCostSection(() -> consumer().transferStationCosts)
				.withEditor(editor)
				.createFields(comp, tk);
	}

}
