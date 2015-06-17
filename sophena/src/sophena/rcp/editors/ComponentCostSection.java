package sophena.rcp.editors;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.ComponentCosts;
import sophena.rcp.Numbers;
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

		Texts.on(t("Investitionskosten", "EUR"))
				.decimal()
				.init(costs().investment)
				.onChanged((s) -> costs().investment = Numbers.read(s));

		Texts.on(t("Nutzungsdauer", "Jahre"))
				.integer()
				.init(costs().duration)
				.onChanged((s) -> costs().duration = Numbers.readInt(s));

		Texts.on(t("Instandsetzung", "%"))
				.decimal()
				.init(costs().repair)
				.onChanged((s) -> costs().repair = Numbers.read(s));

		Texts.on(t("Wartung und Inspektion", "%"))
				.decimal()
				.init(costs().maintenance)
				.onChanged((s) -> costs().maintenance = Numbers.read(s));

		Texts.on(t("Aufwand für Bedienen", "h/a"))
				.decimal()
				.init(costs().operation)
				.onChanged((s) -> costs().operation = Numbers.read(s));
	}

	private Text t(String label, String unit) {
		Text t = UI.formText(composite, toolkit, label);
		UI.formLabel(composite, toolkit, unit);
		return t;
	}
}
