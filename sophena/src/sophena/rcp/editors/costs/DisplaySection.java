package sophena.rcp.editors.costs;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.ProductCosts;
import sophena.model.ProductType;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

/** Cost section where the cost entries are only displayed but not edited. */
class DisplaySection<T> {

	private CostEditor editor;
	private ProductType type;

	Supplier<List<T>> content;
	Function<T, String> label;
	Function<T, ProductCosts> costs;
	Consumer<T> onOpen;

	DisplaySection(CostEditor editor, ProductType type) {
		this.editor = editor;
		this.type = type;
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, Labels.getPlural(type));
		Composite composite = UI.sectionClient(section, tk);
		TableViewer table = createTable(composite);
		table.setLabelProvider(new Label());
		if (content != null)
			table.setInput(content.get());
		if (onOpen != null) {
			Tables.onDoubleClick(table, (e) -> doOpen(table));
			Actions.bind(table, Actions.create("Öffnen", Images.OPEN_16.des(),
					() -> doOpen(table)));
		}
	}

	private void doOpen(TableViewer table) {
		T elem = Viewers.getFirstSelected(table);
		if (elem != null)
			onOpen.accept(elem);
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, Labels.get(type),
				"Investitionskosten", "Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.2, 0.16, 0.16, 0.16, 0.16, 0.16);
		return table;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Labels.getImage(type) : null;
		}

		@Override
		@SuppressWarnings("unchecked")
		public String getColumnText(Object obj, int col) {
			if (costs == null || label == null)
				return "no label def!";
			T t = (T) obj;
			ProductCosts c = costs.apply(t);
			if (c == null)
				return null;
			switch (col) {
			case 0:
				return label.apply(t);
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
