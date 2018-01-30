package sophena.rcp.editors.consumers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.Consumer;
import sophena.model.TimeInterval;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

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
		table.setInput(consumer().interruptions);
		Tables.onDoubleClick(table, (e) -> onEdit());
		return table;
	}

	private void onAdd() {

	}

	private void onEdit() {

	}

	private void onRemove() {

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
				return time.start;
			case 1:
				return time.end;
			case 2:
				return time.description;
			default:
				return null;
			}
		}
	}
}
