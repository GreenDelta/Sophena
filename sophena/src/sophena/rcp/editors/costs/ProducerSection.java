package sophena.rcp.editors.costs;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.ComponentCosts;
import sophena.model.Producer;
import sophena.rcp.Images;
import sophena.rcp.Numbers;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

class ProducerSection {

	private CostEditor editor;

	public ProducerSection(CostEditor editor) {
		this.editor = editor;
	}

	public void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Wärmeerzeuger");
		Composite composite = UI.sectionClient(section, tk);
		TableViewer table = createTable(composite);
		table.setLabelProvider(new Label());
		table.setInput(editor.getProject().producers);
		Tables.onDoubleClick(table, (e) -> openSelected(table));
		Actions.bind(table, Actions.create("Öffnen", Images.OPEN_16.des(),
				() -> openSelected(table)));
	}

	private void openSelected(TableViewer table) {
		Producer p = Viewers.getFirstSelected(table);
		if (p == null)
			return;
		ProducerEditor.open(editor.getProject().toDescriptor(),
				p.toDescriptor());
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Wärmeerzeuger",
				"Investitionskosten", "Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.2, 0.16, 0.16, 0.16, 0.16, 0.16);
		return table;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Images.PRODUCER_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Producer))
				return null;
			Producer p = (Producer) obj;
			ComponentCosts c = p.getCosts();
			if (c == null)
				return null;
			switch (col) {
			case 0:
				return p.name;
			case 1:
				return Numbers.toString(c.investment) + " EUR";
			case 2:
				return Numbers.toString(c.duration) + " a";
			case 3:
				return Numbers.toString(c.repair) + " %";
			case 4:
				return Numbers.toString(c.maintenance) + " %";
			case 5:
				return Numbers.toString(c.operation) + " h/a";
			default:
				return null;
			}
		}

	}

}
