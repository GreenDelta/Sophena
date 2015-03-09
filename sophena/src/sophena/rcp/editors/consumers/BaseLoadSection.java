package sophena.rcp.editors.consumers;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import sophena.model.Consumer;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.BaseLoadWizard;

class BaseLoadSection {

	private ConsumerEditor editor;
	private TableViewer table;

	private BaseLoadSection() {
	}

	static BaseLoadSection of(ConsumerEditor editor) {
		BaseLoadSection section = new BaseLoadSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.getConsumer();
	}

	void create(Composite body, FormToolkit toolkit) {
		Section section = UI.section(body, toolkit, M.BaseLoads);
		Composite composite = UI.sectionClient(section, toolkit);
		table = createTable(composite, toolkit);
		bindActions(section, table);
	}

	private TableViewer createTable(Composite composite, FormToolkit toolkit) {
		TableViewer table = Tables.createViewer(composite, M.Name, M.Power,
				M.Start, M.End);
		Tables.bindColumnWidths(table, 0.25, 0.25, 0.25, 0.25);
		return table;
	}

	private void bindActions(Section section, TableViewer table) {
		Action add = Actions.create(M.Add, Images.ADD_16.des(), () -> {
			BaseLoadWizard.open(consumer());
		});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				() -> {

				});
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(), () -> {

		});
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}
}
