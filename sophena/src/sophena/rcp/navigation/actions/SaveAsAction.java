package sophena.rcp.navigation.actions;

import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.utils.UI;

public class SaveAsAction extends NavigationAction {

	private ProjectDescriptor descriptor;

	public SaveAsAction() {
		setText("Speichern unter...");
		setImageDescriptor(Icon.NEW_PROJECT_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.isEmpty())
			return false;
		var element = elements.getFirst();
		if (!(element instanceof ProjectElement))
			return false;
		ProjectElement e = (ProjectElement) element;
		descriptor = e.content;
		return descriptor != null;
	}

	@Override
	public void run() {
		if (descriptor == null)
			return;
		InputDialog dialog = new InputDialog(UI.shell(), "Speichern unter...",
				"Name des Projekts", descriptor.name + " - Kopie",
				this::checkName);
		if (dialog.open() == Window.OK) {
			String val = dialog.getValue();
			if (val == null)
				return;
			copyProject(val);
		}
	}

	private void copyProject(String val) {
		try {
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(descriptor.id);
			Project copy = p.copy();
			copy.name = val;
			dao.insert(copy);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save project variant", e);
		}
	}

	private String checkName(String name) {
		if (name == null || name.trim().length() == 0)
			return "Der Name darf nicht leer sein";
		// TODO: search in database
		// for (Project var : descriptor.getVariants()) {
		// if (n.equalsIgnoreCase(var.getName()))
		// return "Es existiert schon eine Variante mit diesem Namen";
		// }
		return null;
	}
}
