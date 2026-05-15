package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;

import sophena.model.Boiler;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.rcp.ProductLabel;
import sophena.rcp.app.App;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.SimpleWizard;

class BiogasPlantBoilerWizard extends SimpleWizard {

	private final ProductGroup group;
	private final BiogasPlantBoiler entry;
	private ProductCostSection costSection;

	static int open(BiogasPlantBoiler entry, ProductGroup group) {
		if (entry == null || group == null)
			return Window.CANCEL;
		var wizard = new BiogasPlantBoilerWizard(entry, group);
		return wizard.open();
	}

	private BiogasPlantBoilerWizard(BiogasPlantBoiler entry, ProductGroup group) {
		super("BHKW-Block");
		this.entry = entry;
		this.group = group;
		if (entry.costs == null) {
			entry.costs = new ProductCosts();
		}
	}

	@Override
	protected boolean onFinish() {
		return entry.boiler != null;
	}

	@Override
	protected void create(Composite comp) {
		UI.gridLayout(comp, 3);
		createBoilerCombo(comp);
		costSection = new ProductCostSection(() -> entry.costs).createFields(comp);
	}

	private void createBoilerCombo(Composite comp) {
		var combo = new EntityCombo<Boiler>();
		combo.create("Produkt", comp);
		combo.setLabelProvider(ProductLabel::of);
		combo.setInput(getBoilers(group));
		combo.select(entry.boiler);
		combo.onSelect(boiler -> {
			entry.boiler = boiler;
			ProductCosts.copy(boiler, entry.costs);
			if (costSection != null) {
				costSection.refresh();
			}
		});
		UI.filler(comp);
	}

	private static List<Boiler> getBoilers(ProductGroup group) {
		var boilers = new ArrayList<Boiler>();
		for (var boiler : App.getDb().getAll(Boiler.class)) {
			if (!Objects.equals(boiler.group, group) || !boiler.isCoGenPlant)
				continue;
			boilers.add(boiler);
		}
		Sorters.boilers(boilers);
		return boilers;
	}
}
