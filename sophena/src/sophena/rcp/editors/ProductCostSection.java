package sophena.rcp.editors;

import java.util.function.Supplier;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.ProductCosts;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.Texts.TextDispatch;
import sophena.utils.Num;
import sophena.rcp.utils.UI;

public class ProductCostSection {

	private Supplier<ProductCosts> costs;

	private Editor editor;
	private Composite composite;
	private FormToolkit toolkit;

	public Text investmentText;
	public Text durationText;
	public Text repairTest;
	public Text maintenanceText;
	public Text operationText;

	public ProductCostSection(Supplier<ProductCosts> costs) {
		this.costs = costs;
	}

	public ProductCostSection withEditor(Editor editor) {
		this.editor = editor;
		return this;
	}

	private ProductCosts costs() {
		return costs.get();
	}

	public ProductCostSection createSection(Composite body, FormToolkit tk) {
		Composite composite = UI.formSection(body, tk, "Kosten");
		UI.gridLayout(composite, 3);
		return createFields(composite, tk);
	}

	public ProductCostSection createFields(Composite composite) {
		return createFields(composite, null);
	}

	public ProductCostSection createFields(Composite composite,
			FormToolkit tk) {
		this.composite = composite;
		this.toolkit = tk;

		investmentText = t("Investitionskosten", "EUR", costs().investment)
				.onChanged((s) -> costs().investment = Num.read(s)).text;

		durationText = t("Nutzungsdauer", "Jahre", costs().duration)
				.onChanged((s) -> costs().duration = Num.readInt(s)).text;

		repairTest = t("Instandsetzung", "%", costs().repair)
				.onChanged((s) -> costs().repair = Num.read(s)).text;

		maintenanceText = t("Wartung und Inspektion", "%", costs().maintenance)
				.onChanged((s) -> costs().maintenance = Num.read(s)).text;

		operationText = t("Aufwand für Bedienen", "h/a", costs().operation)
				.onChanged((s) -> costs().operation = Num.read(s)).text;
		return this;
	}

	private TextDispatch t(String label, String unit, double initial) {
		Text t = UI.formText(composite, toolkit, label);
		UI.formLabel(composite, toolkit, unit);
		TextDispatch disp = Texts.on(t).init(initial).decimal();
		if (editor != null)
			disp.onChanged((s) -> editor.setDirty());
		return disp;
	}

	private TextDispatch t(String label, String unit, int initial) {
		Text t = UI.formText(composite, toolkit, label);
		UI.formLabel(composite, toolkit, unit);
		TextDispatch disp = Texts.on(t).init(initial).integer();
		if (editor != null)
			disp.onChanged((s) -> editor.setDirty());
		return disp;
	}

	public void refresh() {
		Texts.set(investmentText, costs().investment);
		Texts.set(durationText, costs().duration);
		Texts.set(repairTest, costs().repair);
		Texts.set(maintenanceText, costs().maintenance);
		Texts.set(operationText, costs().operation);
	}
}
