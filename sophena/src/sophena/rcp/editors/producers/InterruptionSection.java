package sophena.rcp.editors.producers;

import java.util.Collections;
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

import sophena.model.HoursTrace;
import sophena.model.MonthDayHour;
import sophena.model.Producer;
import sophena.model.TimeInterval;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Lists;

class InterruptionSection {

	private ProducerEditor editor;
	private TableViewer table;

	InterruptionSection(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Betriebsunterbrechungen");
		Composite comp = UI.sectionClient(section, tk);
		table = createTable(comp);
		Action add = Actions.create(M.Add,
				Icon.ADD_16.des(), this::onAdd);
		Action remove = Actions.create(M.Remove,
				Icon.DELETE_16.des(), this::onRemove);
		Action edit = Actions.create(M.Edit,
				Icon.EDIT_16.des(), this::onEdit);
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp,
				M.Start, M.End, M.Description);
		Tables.bindColumnWidths(table, 0.3, 0.3, 0.4);
		table.setLabelProvider(new Label());
		Collections.sort(producer().interruptions, (t1, t2) -> {
			int[] i1 = HoursTrace.getHourInterval(t1);
			int[] i2 = HoursTrace.getHourInterval(t2);
			return i1[0] - i2[0];
		});
		table.setInput(producer().interruptions);
		Tables.onDoubleClick(table, (e) -> onEdit());
		return table;
	}

	private void onAdd() {
		TimeInterval time = new TimeInterval();
		time.id = UUID.randomUUID().toString();
		if (InterruptionWizard.open(time) != Window.OK)
			return;
		producer().interruptions.add(time);
		updateUI();
	}

	private void onRemove() {
		List<TimeInterval> list = Viewers.getAllSelected(table);
		list = Lists.findAll(list, producer().interruptions); // JPA
		if (list == null || list.isEmpty())
			return;
		producer().interruptions.removeAll(list);
		updateUI();
	}

	private void onEdit() {
		TimeInterval time = Viewers.getFirstSelected(table);
		time = Lists.find(time, producer().interruptions); // JPA
		if (time == null)
			return;
		if (InterruptionWizard.open(time) != Window.OK)
			return;
		updateUI();
	}

	private void updateUI() {
		table.setInput(producer().interruptions);
		editor.setDirty();
	}

	private class Label extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof TimeInterval))
				return null;
			TimeInterval time = (TimeInterval) obj;
			switch (col) {
			case 0:
				return format(time.start);
			case 1:
				return format(time.end);
			case 2:
				return time.description;
			default:
				return null;
			}
		}

		private String format(String time) {
			if (time == null)
				return "";
			MonthDayHour mdh = MonthDayHour.parse(time);
			return Labels.get(mdh.getMonthDay())
					+ String.format(" %02d:00 Uhr", mdh.getHour());
		}
	}
}