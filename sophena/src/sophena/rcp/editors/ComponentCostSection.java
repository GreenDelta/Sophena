package sophena.rcp.editors;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.ComponentCosts;
import sophena.rcp.utils.DataBinding;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class ComponentCostSection {

	private Supplier<ComponentCosts> costs;

	private DataBinding bind;
	private Composite composite;
	private FormToolkit toolkit;

	public ComponentCostSection(Editor editor, Supplier<ComponentCosts> costs) {
		this.costs = costs;
		this.bind = new DataBinding(editor);
	}

	public void create(Composite body, FormToolkit tk) {
		composite = UI.formSection(body, tk, "Kosten");
		toolkit = tk;
		UI.gridLayout(composite, 3);
		createFields();
	}

	private void createFields() {
		bind.onDouble(
				d("Investitionskosten", "EUR"),
				() -> costs.get()::getInvestment,
				() -> costs.get()::setInvestment);
		bind.onInt(
				i("Nutzungsdauer", "Jahre"),
				() -> costs.get()::getDuration,
				() -> costs.get()::setDuration);
		bind.onDouble(
				d("Instandsetzung", "%"),
				() -> costs.get()::getRepair,
				() -> costs.get()::setRepair);
		bind.onDouble(
				d("Wartung und Inspektion", "%"),
				() -> costs.get()::getMaintenance,
				() -> costs.get()::setMaintenance);
		bind.onDouble(
				d("Aufwand für Bedienen", "h/a"),
				() -> costs.get()::getOperation,
				() -> costs.get()::setOperation);
	}

	private Text d(String label, String unit) {
		Text t = UI.formText(composite, toolkit, label);
		Texts.on(t).decimal();
		UI.formLabel(composite, toolkit, unit);
		return t;
	}

	private Text i(String label, String unit) {
		Text t = UI.formText(composite, toolkit, label);
		Texts.on(t).integer();
		UI.formLabel(composite, toolkit, unit);
		return t;
	}
}
