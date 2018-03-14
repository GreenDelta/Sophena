package sophena.rcp.editors.producers;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.db.daos.FuelDao;
import sophena.math.energetic.CalorificValue;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.fuels.FuelEditor;
import sophena.rcp.utils.Colors;
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
		if (fuel.group != FuelGroup.WOOD)
			createFuelRow(tk, comp);
		else {
			createWoodFuelRow(tk, comp);
			createWaterRow(tk, comp);
		}
		createCalorificValueRow(tk, comp);
		createCostRow(tk, comp);
		createVatRow(tk, comp);
	}

	private void createFuelRow(FormToolkit tk, Composite composite) {
		UI.formLabel(composite, tk, M.Fuel);
		Fuel f = producer().fuelSpec.fuel;
		String text = f.name + " ("
				+ Num.str(f.calorificValue) + " kWh/"
				+ f.unit + ")";
		ImageHyperlink link = new ImageHyperlink(composite, SWT.TOP);
		link.setText(text);
		link.setImage(Icon.FUEL_16.img());
		link.setForeground(Colors.getLinkBlue());
		Controls.onClick(link, e -> FuelEditor.open());
		UI.formLabel(composite, "");
	}

	private void createWoodFuelRow(FormToolkit tk, Composite composite) {
		EntityCombo<Fuel> combo = new EntityCombo<Fuel>();
		combo.create(M.Fuel, composite, tk);
		FuelDao dao = new FuelDao(App.getDb());
		List<Fuel> fuels = dao.getAll().stream().filter((f) -> f.isWood())
				.collect(Collectors.toList());
		Sorters.byName(fuels);
		combo.setInput(fuels);
		Fuel fuel = producer().fuelSpec.fuel;
		combo.select(fuel);
		combo.onSelect((f) -> {
			producer().fuelSpec.fuel = f;
			Texts.set(calorificValueText, Num.intStr(CalorificValue.get(producer())));
			editor.setDirty();
		});
		UI.formLabel(composite, "");
	}

	private void createWaterRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Wassergehalt");
		UI.formLabel(composite, tk, "%");
		Texts.on(t).decimal().required()
				.init(producer().fuelSpec.waterContent)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.waterContent = val;
					Texts.set(calorificValueText, Num.intStr(CalorificValue.get(producer())));
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

	private void createVatRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Mehrwertsteuersatz");
		UI.formLabel(composite, tk, "%");
		Texts.on(t).decimal().required()
				.init(producer().fuelSpec.taxRate)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					producer().fuelSpec.taxRate = val;
					editor.setDirty();
				});
	}

}
