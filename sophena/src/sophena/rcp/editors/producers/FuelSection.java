package sophena.rcp.editors.producers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.db.daos.FuelDao;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Numbers;
import sophena.rcp.editors.basedata.fuels.FuelEditor;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class FuelSection {

	private ProducerEditor editor;

	public FuelSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	private FuelSpec spec() {
		Producer p = editor.getProducer();
		FuelSpec spec = p.fuelSpec;
		if (spec != null)
			return spec;
		spec = new FuelSpec();
		p.fuelSpec = spec;
		return spec;
	}

	public void render(Composite body, FormToolkit tk) {
		Composite composite = UI.formSection(body, tk,
				"Brennstoffspezifikation");
		UI.gridLayout(composite, 3);
		Boiler b = producer().boiler;
		if (b == null)
			return;
		if (b.fuel != null)
			createFuelRow(tk, composite);
		else {
			createWoodFuelRow(tk, composite);
			createWaterRow(tk, composite);
		}
		createCostRow(tk, composite);
		createVatRow(tk, composite);
	}

	private void createFuelRow(FormToolkit tk, Composite composite) {
		UI.formLabel(composite, tk, "Brennstoff");
		Fuel f = producer().boiler.fuel;
		String text = f.name + " ("
				+ Numbers.toString(f.calorificValue) + " kWh/"
				+ f.unit + ")";
		ImageHyperlink link = new ImageHyperlink(composite, SWT.TOP);
		link.setText(text);
		link.setImage(Images.FUEL_16.img());
		link.setForeground(Colors.getLinkBlue());
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				FuelEditor.open();
			}
		});
		UI.formLabel(composite, "");
	}

	private void createWoodFuelRow(FormToolkit tk, Composite composite) {
		EntityCombo<Fuel> combo = new EntityCombo<Fuel>();
		combo.create("Brennstoff", composite, tk);
		FuelDao dao = new FuelDao(App.getDb());
		List<Fuel> fuels = dao.getAll().stream().filter((f) -> f.wood)
				.collect(Collectors.toList());
		Collections.sort(fuels, (f1, f2) -> Strings.compare(f1.name, f2.name));
		combo.setInput(fuels);
		if (spec().woodFuel != null)
			combo.select(spec().woodFuel);
		combo.onSelect((f) -> {
			spec().woodFuel = f;
			editor.setDirty();
		});
		UI.formLabel(composite, "");
	}

	private void createWaterRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Wassergehalt");
		UI.formLabel(composite, tk, "%");
		Texts.on(t).decimal().required()
				.init(spec().waterContent)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					spec().waterContent = val;
					editor.setDirty();
				});
	}

	private void createCostRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Preis (netto)");
		UI.formLabel(composite, tk, "EUR/" + getUnit());
		Texts.on(t).decimal().required()
				.init(spec().pricePerUnit)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					spec().pricePerUnit = val;
					editor.setDirty();
				});
	}

	private String getUnit() {
		Boiler b = producer().boiler;
		if (b == null)
			return "";
		if (b.fuel != null)
			return b.fuel.unit;
		if (b.woodAmountType != null)
			return b.woodAmountType.getUnit();
		else
			return "";
	}

	private void createVatRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Mehrwertsteuersatz");
		UI.formLabel(composite, tk, "%");
		Texts.on(t).decimal().required()
				.init(spec().taxRate)
				.onChanged((s) -> {
					double val = Texts.getDouble(t);
					spec().taxRate = val;
					editor.setDirty();
				});
	}

}
