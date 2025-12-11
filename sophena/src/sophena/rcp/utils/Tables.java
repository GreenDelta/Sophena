package sophena.rcp.utils;

import java.util.function.Consumer;
import java.util.function.ToDoubleFunction;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * A helper class for creating tables, table viewers and related resources.
 */
public class Tables {
	/**
	 * Creates a default table viewer with the given properties. The properties
	 * are also used to create columns where each column label is the respective
	 * property of this column. The viewer is configured in the following way:
	 * <ul>
	 * <li>content provider = {@link ArrayContentProvider}
	 * <li>lines and header are visible
	 * <li>grid data with horizontal and vertical fill
	 *
	 */
	public static TableViewer createViewer(Composite parent,
			String... properties) {
		TableViewer viewer = new TableViewer(parent, SWT.BORDER
				| SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setColumnProperties(properties);
		Table table = viewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		createColumns(table, properties);
		UI.gridData(table, true, true).minimumHeight = 150;
		return viewer;
	}

	public static void createColumns(Table table, String[] labels) {
		for (String label : labels) {
			TableColumn c = new TableColumn(table, SWT.NULL);
			c.setText(label);
		}
		for (TableColumn c : table.getColumns())
			c.pack();
	}

	public static void bindColumnWidths(TableViewer viewer, double... percents) {
		bindColumnWidths(viewer.getTable(), percents);
	}

	/**
	 * Binds the given percentage values (values between 0 and 1) to the column
	 * widths of the given table
	 */
	public static void bindColumnWidths(final Table table,
			final double... percents) {
		if (table == null || percents == null)
			return;
		table.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				double width = table.getSize().x - 25;
				if (width < 50)
					return;
				TableColumn[] columns = table.getColumns();
				for (int i = 0; i < columns.length; i++) {
					if (i >= percents.length)
						break;
					double colWidth = percents[i] * width;
					columns[i].setWidth((int) colWidth);
				}
			}
		});
	}

	public static <T> void sortByLabel(
			Class<T> contentType, TableViewer viewer,
			ITableLabelProvider label, int... cols) {
		var sorters = new TableColumnSorter<?>[cols.length];
		for (int i = 0; i < cols.length; i++) {
			sorters[i] = new TableColumnSorter<>(contentType, cols[i], label);
		}
		registerSorters(viewer, sorters);
	}

	public static <T> void sortByNumber(
			Class<T> type, TableViewer viewer, ToDoubleFunction<T> fn, int col) {
		if (viewer == null || fn == null)
			return;
		var table = viewer.getTable();
		if (col >= table.getColumnCount())
			return;
		var column = table.getColumn(col);
		var ascending = new boolean[]{true};

		column.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (column == table.getSortColumn()) {
					ascending[0] = !ascending[0];
				} else {
					ascending[0] = true;
				}
				table.setSortDirection(ascending[0] ? SWT.UP : SWT.DOWN);
				table.setSortColumn(column);
				viewer.setComparator(new ViewerComparator() {
					@Override
					public int compare(Viewer v, Object a, Object b) {
						double d1 = fn.applyAsDouble(type.cast(a));
						double d2 = fn.applyAsDouble(type.cast(b));
						int cmp = Double.compare(d1, d2);
						return ascending[0] ? cmp : -cmp;
					}
				});
				viewer.refresh();
			}
		});
	}

	private static void registerSorters(
			TableViewer viewer, TableColumnSorter<?>... sorters) {
		if (viewer == null || sorters == null)
			return;
		var table = viewer.getTable();
		int count = table.getColumnCount();
		for (var sorter : sorters) {
			if (sorter.getColumn() >= count)
				continue;
			var column = table.getColumn(sorter.getColumn());
			column.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					TableColumn current = table.getSortColumn();
					if (column == current)
						sorter.setAscending(!sorter.isAscending());
					else
						sorter.setAscending(true);
					int direction = sorter.isAscending() ? SWT.UP : SWT.DOWN;
					table.setSortDirection(direction);
					table.setSortColumn(column);
					viewer.setComparator(sorter);
					viewer.refresh();
				}
			});
		}
	}

	public static void onClick(TableViewer viewer, Consumer<MouseEvent> fn) {
		if (viewer == null || viewer.getTable() == null || fn == null)
			return;
		viewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				fn.accept(e);
			}
		});
	}

	/** Add an event handler for double-clicks on the given table viewer. */
	public static void onDoubleClick(TableViewer viewer,
			Consumer<MouseEvent> handler) {
		if (viewer == null || viewer.getTable() == null || handler == null)
			return;
		viewer.getTable().addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				handler.accept(e);
			}
		});
	}

	/**
	 * Get the table item where the given event occurred. Returns null if the
	 * event occurred in the empty table area.
	 */
	public static TableItem getItem(TableViewer viewer, MouseEvent event) {
		if (viewer == null || event == null)
			return null;
		Table table = viewer.getTable();
		if (table == null)
			return null;
		return table.getItem(new Point(event.x, event.y));
	}

	public static void onDeletePressed(TableViewer viewer,
			Consumer<Event> handler) {
		if (viewer == null || viewer.getTable() == null || handler == null)
			return;
		viewer.getTable().addListener(SWT.KeyUp, (event) -> {
			if (event.keyCode == SWT.DEL) {
				handler.accept(event);
			}
		});
	}

	public static void autoSizeColumns(TableViewer table) {
		if (table != null)
			autoSizeColumns(table.getTable());
	}

	public static void autoSizeColumns(Table table) {
		if (table == null)
			return;
		ControlListener cl = new ControlAdapter() {
			private boolean painted = false;

			@Override
			public void controlResized(ControlEvent e) {
				if (painted) {
					table.removeControlListener(this);
					return;
				}
				for (int i = 0; i < table.getColumnCount(); i++)
					table.getColumn(i).pack();
				painted = true;
			}
		};
		table.addControlListener(cl);
	}

	public static void rightAlignColumns(TableViewer viewer, int... cols) {
		if (viewer != null)
			rightAlignColumns(viewer.getTable(), cols);
	}

	public static void rightAlignColumns(Table table, int... cols) {
		if (table == null)
			return;
		int totalCols = table.getColumnCount();
		for (int col : cols) {
			if (col >= totalCols)
				continue;
			table.getColumn(col).setAlignment(SWT.RIGHT);
		}
	}
}
