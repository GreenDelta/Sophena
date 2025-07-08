package sophena.rcp.editors.producers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.BoilerDao;
import sophena.db.daos.HeatPumpDao;
import sophena.db.daos.SolarCollectorDao;
import sophena.model.Boiler;
import sophena.model.HeatPump;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductType;
import sophena.model.SolarCollector;
import sophena.rcp.App;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class CostSection {

	private final ProducerEditor editor;
	private ProductCostSection costSection;

	CostSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	void create(Composite body, FormToolkit tk) {
		String label = producer().hasProfile() ? "Kosten" : "Produkt";
		Composite comp = UI.formSection(body, tk, label);
		UI.gridLayout(comp, 3);
		if (!producer().hasProfile()) {
			if (producer().productGroup != null && producer().productGroup.type == ProductType.SOLAR_THERMAL_PLANT)
				solarCombo(tk, comp);
			else if (producer().productGroup != null && producer().productGroup.type == ProductType.HEAT_PUMP)
				heatPumpCombo(tk, comp);
			else
				boilerCombo(tk, comp);
			UI.filler(comp);
		}
		costSection = new ProductCostSection(() -> producer().costs)
				.withEditor(editor)
				.createFields(comp, tk);
	}

	private void boilerCombo(FormToolkit tk, Composite comp) {
		EntityCombo<Boiler> combo = new EntityCombo<>();
		combo.create("Bezeichnung", comp, tk);
		combo.setLabelProvider(b -> b.name + " ("
				+ Num.str(b.minPower) + " kW - "
				+ Num.str(b.maxPower) + " kW, η = "
				+ Num.str(b.efficiencyRate * 100d) + "%)");
		Boiler b = producer().boiler;
		if (b == null)
			return;
		combo.setInput(getPossibleBoilers(b));
		combo.select(b);
		combo.onSelect(boiler -> {
			producer().boiler = boiler;
			ProductCosts.copy(boiler, producer().costs);
			costSection.refresh();
			editor.setDirty();
		});
	}

	private List<Boiler> getPossibleBoilers(Boiler b) {
		if (b == null || b.group == null)
			return Collections.emptyList();
		BoilerDao dao = new BoilerDao(App.getDb());
		List<Boiler> all = dao.getAll();
		List<Boiler> filtered = new ArrayList<>();
		for (Boiler other : all) {
			if (Objects.equals(b.group, other.group)) {
				filtered.add(other);
			}
		}
		Sorters.boilers(filtered);
		return filtered;
	}

	private void solarCombo(FormToolkit tk, Composite comp) {
		EntityCombo<SolarCollector> combo = new EntityCombo<>();
		combo.create("Bezeichnung", comp, tk);
		combo.setLabelProvider(s -> s.name + " (" + s.collectorArea + " m2)");
		SolarCollector s = producer().solarCollector;
		if (s == null)
			return;
		combo.setInput(getPossibleSolarCollectors(s));
		combo.select(s);
		combo.onSelect(solarCollector -> {
			producer().solarCollector = solarCollector;
			ProductCosts.copy(solarCollector, producer().costs);
			costSection.refresh();
			editor.setDirty();
		});
	}

	private void heatPumpCombo(FormToolkit tk, Composite comp) {
		EntityCombo<HeatPump> combo = new EntityCombo<>();
		combo.create("Bezeichnung", comp, tk);
		combo.setLabelProvider(h -> h.name + " (" + h.ratedPower + " kW)");
		HeatPump h = producer().heatPump;
		if (h == null)
			return;
		combo.setInput(getPossibleHeatPumps(h));
		combo.select(h);
		combo.onSelect(heatPump -> {
			producer().heatPump = heatPump;
			ProductCosts.copy(heatPump, producer().costs);
			costSection.refresh();
			editor.setDirty();
		});
	}

	private List<SolarCollector> getPossibleSolarCollectors(SolarCollector s) {
		if (s == null || s.group == null)
			return Collections.emptyList();
		SolarCollectorDao dao = new SolarCollectorDao(App.getDb());
		List<SolarCollector> all = dao.getAll();
		List<SolarCollector> filtered = new ArrayList<>();
		for (SolarCollector other : all) {
			if (Objects.equals(s.group, other.group)) {
				filtered.add(other);
			}
		}
		Sorters.solarCollectors(filtered);
		return filtered;
	}

	private List<HeatPump> getPossibleHeatPumps(HeatPump h) {
		if (h == null || h.group == null)
			return Collections.emptyList();
		HeatPumpDao dao = new HeatPumpDao(App.getDb());
		List<HeatPump> all = dao.getAll();
		List<HeatPump> filtered = new ArrayList<>();
		for (HeatPump other : all) {
			if (Objects.equals(h.group, other.group)) {
				filtered.add(other);
			}
		}
		Sorters.heatPumps(filtered);
		return filtered;
	}

}
