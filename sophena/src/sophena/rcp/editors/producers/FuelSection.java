package sophena.rcp.editors.producers;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.FuelDao;
import sophena.math.energetic.CalorificValue;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.WoodAmountType;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/**
 * A producer must have always a fuel specification which can be edited in this
 * section.
 */
class FuelSection {

	private ProducerEditor editor;
	private Text calorificValueText;
	private Label calorificValueUnit;
	private Label priceUnit;
	private Label ashCostsUnit;

	FuelSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	void render(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk,
				"Brennstoffspezifikation");
		UI.gridLayout(comp, 3);
		FuelSpec spec = producer().fuelSpec;
		if (spec == null || spec.fuel == null)
			return;
		Fuel fuel = spec.fuel;
		createFuelRows(tk, comp);
		if (fuel.group == FuelGroup.WOOD) {
			createWoodUnitRow(tk, comp);
			createWaterRow(tk, comp);
		}
		createCalorificValueRow(tk, comp);
		createCostRow(tk, comp);
		createAshCostRow(tk, comp);
	}

	private void createFuelRows(FormToolkit tk, Composite comp) {
		Fuel fuel = producer().fuelSpec.fuel;
		EntityCombo<Fuel> combo = new EntityCombo<>();
		combo.create(M.Fuel, comp, tk);
		FuelDao dao = new FuelDao(App.getDb());
		List<Fuel> fuels = dao.getAll().stream()
				.filter((f) -> f.group == fuel.group)
				.collect(Collectors.toList());
		Sorters.byName(fuels);
		combo.setInput(fuels);
		combo.select(fuel);
		combo.onSelect(this::onFuelChange);
		UI.formLabel(comp, "");
	}

	private void onFuelChange(Fuel f) {
		if (f == null)
			return;
		Producer p = producer();
		p.fuelSpec.fuel = f;
		String unit = Labels.getFuelUnit(p);
		Texts.set(calorificValueText,
				Num.intStr(CalorificValue.get(p.fuelSpec)));
		priceUnit.setText("EUR/" + unit);
		calorificValueUnit.setText("kWh/" + unit);
		if (ashCostsUnit != null)
			ashCostsUnit.setText("EUR/" + (f.isWood() ? "t" : f.unit));
		calorificValueUnit.getParent().layout();
		editor.setDirty();
	}

	private void createWoodUnitRow(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, "Referenzeinheit");
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 2);
		WoodAmountType current = producer().fuelSpec.woodAmountType;
		Button chips = tk.createButton(inner, WoodAmountType.CHIPS.getUnit()
				+ " (Schüttraummeter)", SWT.RADIO);
		chips.setSelection(current == WoodAmountType.CHIPS);
		Controls.onSelect(chips, e -> {
			FuelSpec spec = producer().fuelSpec;
			spec.woodAmountType = WoodAmountType.CHIPS;
			onFuelChange(spec.fuel); // updates unit labels etc.
		});
		Button mass = tk.createButton(inner, WoodAmountType.MASS.getUnit()
				+ " (Tonnen)", SWT.RADIO);
		Controls.onSelect(mass, e -> {
			FuelSpec spec = producer().fuelSpec;
			spec.woodAmountType = WoodAmountType.MASS;
			onFuelChange(spec.fuel); // updates unit labels etc.
		});
		mass.setSelection(current == WoodAmountType.MASS);
		UI.filler(comp);
	}

	private void createWaterRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, tk, "Wassergehalt");
		UI.formLabel(comp, tk, "%");
		Texts.on(t).decimal().required()
				.init(producer().fuelSpec.waterContent)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.waterContent = val;
					FuelSpec spec = producer().fuelSpec;
					Texts.set(calorificValueText,
							Num.intStr(CalorificValue.get(spec)));
					editor.setDirty();
				});
	}

	private void createCalorificValueRow(FormToolkit tk, Composite comp) {
		calorificValueText = UI.formText(comp, tk, "Heizwert");
		calorificValueUnit = UI.formLabel(comp, tk,
				"kWh/" + Labels.getFuelUnit(producer()));
		Texts.on(calorificValueText).decimal().calculated()
				.init(Num.intStr(CalorificValue.get(producer().fuelSpec)));
	}

	private void createCostRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Preis");
		priceUnit = UI.formLabel(composite, tk,
				"EUR/" + Labels.getFuelUnit(producer()));
		Texts.on(t).decimal().required()
				.init(producer().fuelSpec.pricePerUnit)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.pricePerUnit = val;
					editor.setDirty();
				});
	}

	private void createAshCostRow(FormToolkit tk, Composite comp) {
		FuelSpec spec = producer().fuelSpec;
		if (spec == null || spec.fuel == null)
			return;
		Fuel fuel = spec.fuel;
		if (fuel.ashContent < 1e-6)
			return;
		Text t = UI.formText(comp, tk, "Ascheentsorgungskosten");
		String unit = fuel.isWood() ? "t" : fuel.unit;
		ashCostsUnit = UI.formLabel(comp, tk, "EUR/" + unit);
		Texts.on(t).decimal()
				.init(spec.ashCosts)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.ashCosts = val;
					editor.setDirty();
				});
	}
}
