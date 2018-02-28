package sophena.rcp.editors.consumers;

import java.time.MonthDay;
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

import sophena.model.Consumer;
import sophena.model.HoursTrace;
import sophena.model.TimeInterval;
import sophena.rcp.Icon;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Lists;

class InterruptionSection {

	private ConsumerEditor editor;
	private TableViewer table;

	private InterruptionSection() {
	}

	static InterruptionSection of(ConsumerEditor editor) {
		InterruptionSection section = new InterruptionSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.consumer;
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
		Collections.sort(consumer().interruptions, (t1, t2) -> {
			int[] i1 = HoursTrace.getDayInterval(t1);
			int[] i2 = HoursTrace.getDayInterval(t2);
			return i1[0] - i2[0];
		});
		table.setInput(consumer().interruptions);
		Tables.onDoubleClick(table, (e) -> onEdit());
		return table;
	}

	private void onAdd() {
		TimeInterval time = new TimeInterval();
		time.id = UUID.randomUUID().toString();
		if (InterruptionWizard.open(time) != Window.OK)
			return;
		consumer().interruptions.add(time);
		updateUI();
	}

	private void onEdit() {
		TimeInterval time = Viewers.getFirstSelected(table);
		time = Lists.find(time, consumer().interruptions); // JPA
		if (time == null)
			return;
		if (InterruptionWizard.open(time) != Window.OK)
			return;
		updateUI();
	}

	private void onRemove() {
		List<TimeInterval> list = Viewers.getAllSelected(table);
		list = Lists.findAll(list, consumer().interruptions); // JPA
		if (list == null || list.isEmpty())
			return;
		consumer().interruptions.removeAll(list);
		updateUI();
	}

	private void updateUI() {
		table.setInput(consumer().interruptions);
		editor.calculate();
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
			try {
				MonthDay md = MonthDay.parse(time);
				return Labels.get(md);
			} catch (Exception e) {
				Log.error(this, "failed to parse time " + time, e);
				return "<ERROR>";
			}
		}
	}
}
