package sophena.rcp.navigation.actions;

import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;

import sophena.db.daos.ProjectFolderDao;
import sophena.model.ProjectFolder;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

public class NewFolderAction extends Action {

	public NewFolderAction() {
		setText("Neuer Ordner");
		setImageDescriptor(Icon.FOLDER_16.des());
	}

	@Override
	public void run() {
		ProjectFolderDao dao = new ProjectFolderDao(App.getDb());
		InputDialog dialog = new InputDialog(UI.shell(), "Neuer Ordner",
				"Name des Ordners:", "Neuer Ordner", name -> {
					if (name == null || name.trim().length() == 0)
						return "Der Name darf nicht leer sein";
					boolean exists = dao.getAll().stream()
							.filter(f -> Strings.nullOrEqual(f.name, name))
							.findFirst()
							.isPresent();
					if (exists)
						return "Ein Ordner mit dem Namen '" + name
								+ "' existiert bereits";
					return null;
				});
		if (dialog.open() != Window.OK)
			return;
		String name = dialog.getValue();
		ProjectFolder folder = new ProjectFolder();
		folder.id = UUID.randomUUID().toString();
		folder.name = name;
		dao.insert(folder);
		Navigator.refresh();
	}
}
