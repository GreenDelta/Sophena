package sophena.rcp.editors.producers;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.FuelDao;
import sophena.math.energetic.CalorificValue;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
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
			createWaterRow(tk, comp);
		}
		createCalorificValueRow(tk, comp);
		createCostRow(tk, comp);
		createVatRow(tk, comp);
	}

	private void createFuelRows(FormToolkit tk, Composite comp) {
		Fuel fuel = producer().fuelSpec.fuel;
		EntityCombo<Fuel> combo = new EntityCombo<Fuel>();
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
		// TODO: update labels
		producer().fuelSpec.fuel = f;
		Texts.set(calorificValueText,
				Num.intStr(CalorificValue.get(producer())));
		editor.setDirty();
	}

	private void createWaterRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, tk, "Wassergehalt");
		UI.formLabel(comp, tk, "%");
		Texts.on(t).decimal().required()
				.init(producer().fuelSpec.waterContent)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.waterContent = val;
					Texts.set(calorificValueText,
							Num.intStr(CalorificValue.get(producer())));
					editor.setDirty();
				});
	}

	private void createCalorificValueRow(FormToolkit tk, Composite composite) {
		calorificValueText = UI.formText(composite, tk, "Heizwert");
		UI.formLabel(composite, tk, "kWh/" + Labels.getFuelUnit(producer()));
		Texts.on(calorificValueText).decimal().calculated()
				.init(Num.intStr(CalorificValue.get(producer())));
	}

	private void createCostRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Preis (netto)");
		UI.formLabel(composite, tk, "EUR/" + Labels.getFuelUnit(producer()));
		Texts.on(t).decimal().required()
				.init(producer().fuelSpec.pricePerUnit)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.pricePerUnit = val;
					editor.setDirty();
				});
	}

	private void createVatRow(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, tk, "Mehrwertsteuersatz");
		UI.formLabel(comp, tk, "%");
		Texts.on(t).decimal().required()
				.init(producer().fuelSpec.taxRate)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.taxRate = val;
					editor.setDirty();
				});
	}

}
