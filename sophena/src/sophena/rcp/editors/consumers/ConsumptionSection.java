package sophena.rcp.editors.consumers;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.math.energetic.EfficiencyRate;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class ConsumptionSection {

	private ConsumerEditor editor;
	private TableViewer table;

	private ConsumptionSection() {
	}

	static ConsumptionSection of(ConsumerEditor editor) {
		ConsumptionSection section = new ConsumptionSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.consumer;
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, M.ConsumptionData);
		Composite composite = UI.sectionClient(section, tk);
		table = createTable(composite);
		Action add = Actions.create(M.Add,
				Icon.ADD_16.des(), this::onAdd);
		Action remove = Actions.create(M.Remove,
				Icon.DELETE_16.des(), this::onRemove);
		Action edit = Actions.create(M.Edit,
				Icon.EDIT_16.des(), this::onEdit);
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite composite) {
		TableViewer table = Tables.createViewer(composite, M.Fuel,
				M.Consumption,
				"Genutzte Wärme");
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		table.setLabelProvider(new Label());
		table.setInput(consumer().fuelConsumptions);
		Tables.onDoubleClick(table, (e) -> onEdit());
		return table;
	}

	private void onAdd() {
		FuelConsumption c = new FuelConsumption();
		c.id = UUID.randomUUID().toString();
		int code = ConsumptionWizard.open(c, consumer().loadHours);
		if (code == Window.OK) {
			consumer().fuelConsumptions.add(c);
			updateUI();
		}
	}

	private void onRemove() {
		List<FuelConsumption> list = Viewers.getAllSelected(table);
		if (list == null || list.isEmpty())
			return;
		consumer().fuelConsumptions.removeAll(list);
		updateUI();
	}

	private void onEdit() {
		FuelConsumption c = Viewers.getFirstSelected(table);
		if (c == null)
			return;
		int code = ConsumptionWizard.open(c, consumer().loadHours);
		if (code == Window.OK) {
			updateUI();
		}
	}

	private void updateUI() {
		table.setInput(consumer().fuelConsumptions);
		editor.calculate();
		editor.setDirty();
	}

	private class Label extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Icon.FUEL_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof FuelConsumption c))
				return null;
			return switch (col) {
				case 0 -> c.fuel != null ? c.fuel.name : null;
				case 1 -> getAmount(c);
				case 2 -> getUsedHeat(c);
				default -> null;
			};
		}

		private String getAmount(FuelConsumption c) {
			if (c == null || c.fuel == null)
				return null;
			String amount = Num.str(c.amount);
			Fuel fuel = c.fuel;
			if (!fuel.isWood())
				return amount + " " + fuel.unit;
			String unit;
			if (c.woodAmountType == null)
				unit = "?";
			else
				unit = c.woodAmountType.getUnit();
			String wc = Num.str(c.waterContent);
			return amount + " " + unit + " (Wgh.: " + wc + "%)";
		}

		private String getUsedHeat(FuelConsumption c) {
			if (c == null)
				return null;
			String heat = Num.intStr(c.getUsedHeat()) + " kWh";
			double eta = EfficiencyRate.get(c.utilisationRate, consumer().loadHours);
			return heat + " (η=" + Num.str(eta) + "%)";
		}
	}
}
