package sophena.rcp.editors.producers;

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
import sophena.model.Producer;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Numbers;
import sophena.rcp.editors.basedata.fuels.FuelEditor;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.UI;

class FuelSection {

	private ProducerEditor editor;

	public FuelSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	public void render(Composite body, FormToolkit tk) {
		Composite composite = UI.formSection(body, tk,
				"Brennstoffspezifikation");
		UI.gridLayout(composite, 3);
		Boiler b = producer().getBoiler();
		if (b == null)
			return;
		if (b.getFuel() != null)
			createFuelRow(tk, composite);
		else
			createWoodFuelRow(tk, composite);
		createCostRow(tk, composite);
		createVatRow(tk, composite);
	}

	private void createFuelRow(FormToolkit tk, Composite composite) {
		UI.formLabel(composite, tk, "Brennstoff");
		Fuel f = producer().getBoiler().getFuel();
		String text = f.getName() + " ("
				+ Numbers.toString(f.getCalorificValue()) + " kWh/"
				+ f.getUnit() + ")";
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
		List<Fuel> fuels = dao.getAll().stream()
				.filter((f) -> f.isWood())
				.collect(Collectors.toList());
		combo.setInput(fuels);
		combo.onSelect((f) -> {
			// producer.setWoodFuel();
			editor.setDirty();
		});
		UI.formLabel(composite, "");
	}

	private void createCostRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Preis (netto)");
		UI.formLabel(composite, tk, "EUR/" + getUnit());
	}

	private String getUnit() {
		Boiler b = producer().getBoiler();
		if (b == null)
			return "";
		if (b.getFuel() != null)
			return b.getFuel().getUnit();
		if (b.getWoodAmountType() != null)
			return b.getWoodAmountType().getUnit();
		else
			return "";
	}

	private void createVatRow(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Mehrwertsteuersatz");
		UI.formLabel(composite, tk, "%");
	}

}
