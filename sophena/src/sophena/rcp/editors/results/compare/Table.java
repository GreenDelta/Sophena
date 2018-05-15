package sophena.rcp.editors.results.compare;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntFunction;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import sophena.calc.Comparison;
import sophena.rcp.utils.Tables;

class Table {

	private final Comparison comparinson;
	private final List<Item> items = new ArrayList<>();

	Table(Comparison comparinson) {
		this.comparinson = comparinson;
	}

	void render(Composite comp) {
		int length = comparinson.projects.length;
		String[] headers = new String[length + 1];
		headers[0] = "";
		int[] aligns = new int[length];
		double[] widths = new double[headers.length];
		double width = 1.0 / headers.length;
		widths[0] = width;
		for (int i = 0; i < length; i++) {
			String name = comparinson.projects[i].name;
			name = name == null ? "?" : name;
			headers[i + 1] = name;
			widths[i + 1] = width;
			aligns[i] = i + 1;
		}
		TableViewer table = Tables.createViewer(comp, headers);
		Tables.bindColumnWidths(table, widths);
		table.setLabelProvider(new Label());
		Tables.rightAlignColumns(table, aligns);
		table.setInput(items);
	}

	void row(String header, IntFunction<String> fn) {
		Item item = new Item();
		item.aspect = header;
		int length = comparinson.projects.length;
		item.results = new String[length];
		for (int i = 0; i < length; i++) {
			item.results[i] = fn.apply(i);
		}
		items.add(item);
	}

	void emptyRow() {
		Item item = new Item();
		item.aspect = "";
		int length = comparinson.projects.length;
		item.results = new String[length];
		for (int i = 0; i < length; i++) {
			item.results[i] = "";
		}
		items.add(item);
	}

	private class Item {
		String aspect;
		String[] results;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item))
				return null;
			Item item = (Item) obj;
			if (col == 0)
				return item.aspect;
			int idx = col - 1;
			if (item.results == null || idx >= item.results.length)
				return null;
			return item.results[idx];
		}
	}
}
