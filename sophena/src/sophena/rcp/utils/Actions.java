package sophena.rcp.utils;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.forms.widgets.Section;

public class Actions {

	private Actions() {
	}

	public static Action create(String title, Runnable fn) {
		return create(title, null, fn);
	}

	public static Action create(String title,
			ImageDescriptor image, Runnable fn) {
		return new Action() {
			{
				if (title != null) {
					setText(title);
					setToolTipText(title);
				}
				if (image != null) {
					setImageDescriptor(image);
				}
			}

			@Override
			public void run() {
				if (fn != null) {
					fn.run();
				}
			}
		};
	}

	/**
	 * Creates a context menu with the given actions on the table viewer.
	 */
	public static void bind(TableViewer viewer, Action... actions) {
		Table table = viewer.getTable();
		if (table == null)
			return;
		MenuManager menu = new MenuManager();
		for (Action action : actions)
			menu.add(action);
		table.setMenu(menu.createContextMenu(table));
	}

	/**
	 * Creates buttons for the given actions in a section tool-bar.
	 */
	public static void bind(Section section, Action... actions) {
		var toolBar = new ToolBarManager();
		for (var action : actions) {
			toolBar.add(action);
		}
		var control = toolBar.createControl(section);
		// we need to set the background color of the toolbar
		// here, otherwise it is grey on Windows; not sure if
		// this works with dark mode though
		control.setBackground(section.getBackground());
		section.setTextClient(control);
	}

}
