package sophena.rcp.editors.basedata.fuels;

import java.util.Objects;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import sophena.Labels;
import sophena.db.daos.ProducerDao;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.Producer;
import sophena.rcp.App;
import sophena.rcp.utils.UI;

class FuelGroupCombo {

	private final Combo combo;
	private final Fuel fuel;
	private final FuelGroup[] groups;

	private FuelGroupCombo(Fuel fuel, Combo combo) {
		this.fuel = fuel;
		this.combo = combo;
		FuelGroup[] allGroups = FuelGroup.values();
		groups = new FuelGroup[allGroups.length - 1];
		int i = 0;
		for (FuelGroup g : allGroups) {
			if (g == FuelGroup.WOOD)
				continue;
			groups[i] = g;
			i++;
		}
		this.init();
	}

	static FuelGroupCombo on(Fuel fuel, Composite comp) {
		Combo combo = UI.formCombo(comp, "Gruppe");
		FuelGroupCombo self = new FuelGroupCombo(fuel, combo);
		UI.filler(comp);
		return self;
	}

	FuelGroup getSelected() {
		if (combo == null || groups == null)
			return null;
		int i = combo.getSelectionIndex();
		if (i >= groups.length)
			return null;
		return groups[i];
	}

	private void init() {
		if (!canEdit()) {
			String item = Labels.get(fuel.group);
			combo.setItems(new String[] { item });
			combo.select(0);
			combo.setEnabled(false);
			return;
		}
		String[] items = new String[groups.length];
		int selectedIdx = 0;
		FuelGroup selected = fuel != null ? fuel.group : null;
		for (int i = 0; i < items.length; i++) {
			FuelGroup g = groups[i];
			items[i] = Labels.get(g);
			if (g == selected) {
				selectedIdx = i;
			}
		}
		combo.setItems(items);
		combo.select(selectedIdx);
	}

	/**
	 * It is not allowed to edit a fuel that is used in a fuel specification of
	 * a producer.
	 */
	private boolean canEdit() {
		if (fuel == null || fuel.group == null)
			return true;
		if (fuel.isProtected)
			return false;
		ProducerDao dao = new ProducerDao(App.getDb());
		for (Producer p : dao.getAll()) {
			if (p.fuelSpec == null)
				continue;
			if (Objects.equals(p.fuelSpec.fuel, fuel))
				return false;
		}
		return true;
	}

}
