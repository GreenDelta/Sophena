package sophena.rcp.editors.producers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.BoilerDao;
import sophena.model.Boiler;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.rcp.App;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class CostSection {

	private ProducerEditor editor;
	private ProductCostSection costSection;

	CostSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	void create(Composite body, FormToolkit tk) {
		String label = producer().hasProfile() ? "Kosten" : "Heizkessel";
		Composite comp = UI.formSection(body, tk, label);
		UI.gridLayout(comp, 3);
		if (!producer().hasProfile()) {
			boilerCombo(tk, comp);
			UI.filler(comp);
		}
		costSection = new ProductCostSection(() -> producer().costs)
				.withEditor(editor)
				.createFields(comp, tk);
	}

	private void boilerCombo(FormToolkit tk, Composite comp) {
		EntityCombo<Boiler> combo = new EntityCombo<>();
		combo.create("Produkt", comp, tk);
		combo.setLabelProvider(b -> b.name + " ("
				+ Num.str(b.minPower) + " kW - "
				+ Num.str(b.maxPower) + " kW, \u03B7 = "
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

}
