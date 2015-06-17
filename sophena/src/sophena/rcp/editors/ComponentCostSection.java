package sophena.rcp.editors;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.ComponentCosts;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.Texts.TextDispatch;
import sophena.rcp.utils.UI;

public class ComponentCostSection {

	private Supplier<ComponentCosts> costs;

	private Editor editor;
	private Composite composite;
	private FormToolkit toolkit;

	public ComponentCostSection(Editor editor, Supplier<ComponentCosts> costs) {
		this.costs = costs;
		this.editor = editor;
	}

	private ComponentCosts costs() {
		return costs.get();
	}

	public void create(Composite body, FormToolkit tk) {
		composite = UI.formSection(body, tk, "Kosten");
		toolkit = tk;
		UI.gridLayout(composite, 3);
		createFields();
	}

	private void createFields() {
		t("Investitionskosten", "EUR", costs().investment)
				.onChanged((s) -> costs().investment = Numbers.read(s));

		t("Nutzungsdauer", "Jahre", costs().duration)
				.onChanged((s) -> costs().duration = Numbers.readInt(s));

		t("Instandsetzung", "%", costs().repair)
				.onChanged((s) -> costs().repair = Numbers.read(s));

		t("Wartung und Inspektion", "%", costs().maintenance)
				.onChanged((s) -> costs().maintenance = Numbers.read(s));

		t("Aufwand für Bedienen", "h/a", costs().operation)
				.onChanged((s) -> costs().operation = Numbers.read(s));
	}

	private TextDispatch t(String label, String unit, double initial) {
		Text t = UI.formText(composite, toolkit, label);
		UI.formLabel(composite, toolkit, unit);
		return Texts.on(t)
				.init(initial)
				.decimal()
				.onChanged((s) -> editor.setDirty());
	}

	private TextDispatch t(String label, String unit, int initial) {
		Text t = UI.formText(composite, toolkit, label);
		UI.formLabel(composite, toolkit, unit);
		return Texts.on(t)
				.init(initial)
				.integer()
				.onChanged((s) -> editor.setDirty());
	}
}
