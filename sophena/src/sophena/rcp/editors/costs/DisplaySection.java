package sophena.rcp.editors.costs;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.Labels;
import sophena.model.ProductCosts;
import sophena.model.ProductType;
import sophena.rcp.Icon;
import sophena.rcp.Images;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpBox;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Lists;
import sophena.utils.Num;

/** Cost section where the cost entries are only displayed but not edited. */
class DisplaySection<T> {

	private ProductType type;

	Supplier<List<T>> content;
	Function<T, String> label;
	Function<T, ProductCosts> costs;
	Consumer<T> onOpen;

	private TableViewer table;

	DisplaySection(ProductType type) {
		this.type = type;
	}

	void create(Composite body, FormToolkit tk) {
		Section section;
		if (content != null && Lists.nullOrEmpty(content.get())) {
			section = UI.collapsedSection(body, tk, Labels.getPlural(type));
		} else {
			section = UI.section(body, tk, Labels.getPlural(type));
		}
		Composite composite = UI.sectionClient(section, tk);
		table = createTable(composite);
		table.setLabelProvider(new Label());
		Tables.onDoubleClick(table, e -> doOpen(table));
		Action open = Actions.create("Öffnen", Icon.OPEN_16.des(),
				() -> doOpen(table));
		if (type == ProductType.COGENERATION_PLANT) {
			Action help = Actions.create("Hilfe", Icon.INFO_16.des(),
					() -> HelpBox.show("KWK-Anlagen", H.CoGenPlants));
			Actions.bind(section, help);
		}
		Actions.bind(table, open);
		refresh();
	}

	private void doOpen(TableViewer table) {
		T elem = Viewers.getFirstSelected(table);
		if (elem != null && onOpen != null) {
			onOpen.accept(elem);
		}
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Produkt",
				"Investitionskosten", "Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.2, 0.16, 0.16, 0.16, 0.16, 0.16);
		return table;
	}

	void refresh() {
		if (content == null)
			return;
		List<T> list = content.get();
		if (list == null)
			return;
		table.setInput(content.get());
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Images.getImage(type) : null;
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
				return Num.str(c.investment) + " EUR";
			case 2:
				return Num.intStr(c.duration) + " a";
			case 3:
				return Num.str(c.repair) + " %";
			case 4:
				return Num.str(c.maintenance) + " %";
			case 5:
				return Num.str(c.operation) + " h/a";
			default:
				return null;
			}
		}
	}
}
