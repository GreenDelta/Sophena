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

import sophena.calc.BoilerEfficiency;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

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
		return editor.getConsumer();
	}

	void create(Composite body, FormToolkit toolkit) {
		Section section = UI.section(body, toolkit, M.ConsumptionData);
		Composite composite = UI.sectionClient(section, toolkit);
		table = createTable(composite);
		Action add = Actions.create(M.Add, Images.ADD_16.des(), this::onAdd);
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				this::onRemove);
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
				this::onEdit);
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite composite) {
		TableViewer table = Tables.createViewer(composite, M.Fuel, M.Consumption,
				"Genutzte Wärme");
		Tables.bindColumnWidths(table, 0.4, 0.3, 0.3);
		table.setLabelProvider(new Label());
		table.setInput(consumer().getFuelConsumptions());
		Tables.onDoubleClick(table, (e) -> onEdit());
		return table;
	}

	private void onAdd() {
		FuelConsumption c = new FuelConsumption();
		c.setId(UUID.randomUUID().toString());
		int code = ConsumptionDataWizard.open(c, consumer()
				.getLoadHours());
		if (code == Window.OK) {
			consumer().getFuelConsumptions().add(c);
			table.setInput(consumer().getFuelConsumptions());
			editor.calculate();
			editor.setDirty();
		}
	}

	private void onRemove() {
		List<FuelConsumption> list = Viewers.getAllSelected(table);
		if (list == null || list.isEmpty())
			return;
		consumer().getFuelConsumptions().removeAll(list);
		table.setInput(consumer().getFuelConsumptions());
		editor.calculate();
		editor.setDirty();
	}

	private void onEdit() {
		FuelConsumption c = Viewers.getFirstSelected(table);
		if (c == null)
			return;
		int code = ConsumptionDataWizard.open(c, consumer()
				.getLoadHours());
		if (code == Window.OK) {
			table.setInput(consumer().getFuelConsumptions());
			editor.calculate();
			editor.setDirty();
		}
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			return col == 0 ? Images.FUEL_16.img() : null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof FuelConsumption))
				return null;
			FuelConsumption c = (FuelConsumption) element;
			switch (col) {
			case 0:
				return c.getFuel() != null ? c.getFuel().getName() : null;
			case 1:
				return getAmount(c);
			case 2:
				return getUsedHeat(c);
			default:
				return null;
			}
		}

		private String getAmount(FuelConsumption c) {
			if (c == null || c.getFuel() == null)
				return null;
			String amount = Numbers.toString(c.getAmount());
			Fuel fuel = c.getFuel();
			if (!fuel.isWood())
				return amount + " " + fuel.getUnit();
			String unit;
			if (c.getWoodAmountType() == null)
				unit = "?";
			else
				unit = c.getWoodAmountType().getUnit();
			String wc = Numbers.toString(c.getWaterContent());
			return amount + " " + unit + " (Wgh.: " + wc + "%)";
		}

		private String getUsedHeat(FuelConsumption c) {
			if (c == null)
				return null;
			String heat = Numbers.toString(c.getUsedHeat()) + " kWh";
			double eta = BoilerEfficiency.getEfficiencyRate(
					c.getUtilisationRate(), consumer().getLoadHours());
			return heat + " (\u03B7=" + Numbers.toString(eta) + "%)";
		}
	}
}
