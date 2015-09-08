package sophena.rcp.editors.costs;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.ProductType;
import sophena.model.Project;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class ProductSection {

	private CostEditor editor;
	private ProductType type;

	ProductSection(CostEditor editor, ProductType type) {
		this.editor = editor;
		this.type = type;
	}

	private Project project() {
		return editor.getProject();
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, Labels.getPlural(type));
		Composite composite = UI.sectionClient(section, tk);
		TableViewer table = createTable(composite);
		Action add = Actions.create(M.Add, Images.ADD_16.des(), () -> {
		});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				() -> {
				});
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
				() -> {
				});
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Komponente",
				"Investitionskosten", "Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.2, 0.16, 0.16, 0.16, 0.16, 0.16);
		return table;
	}

}
