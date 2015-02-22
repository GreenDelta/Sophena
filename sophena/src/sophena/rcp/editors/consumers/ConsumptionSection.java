package sophena.rcp.editors.consumers;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.UI;

class ConsumptionSection {

	private ConsumerEditor editor;

	private ConsumptionSection() {
	}

	static ConsumptionSection of(ConsumerEditor editor) {
		ConsumptionSection section = new ConsumptionSection();
		section.editor = editor;
		return section;
	}

	void create(Composite body, FormToolkit toolkit) {
		Section section = UI.section(body, toolkit, M.ConsumptionData);
		Composite composite = UI.sectionClient(section, toolkit);
		bindActions(section);
	}

	private void bindActions(Section section) {
		Action add = Actions.create(M.Add, Images.ADD_16.des(), () -> {

		});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(), () -> {

		});
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(), () -> {

		});
		Actions.bind(section, add, edit, remove);
	}

}
