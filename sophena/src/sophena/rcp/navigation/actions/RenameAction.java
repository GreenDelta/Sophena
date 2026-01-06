package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectFolderDao;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

public class RenameAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;
	private Method handler;

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.isEmpty())
			return false;
		var element = elements.getFirst();
		handler = Handlers.find(element, this);
		if (handler == null) {
			elem = null;
			return false;
		} else {
			elem = element;
			return true;
		}
	}

	public RenameAction() {
		setText("Umbenennen");
		setImageDescriptor(Icon.RENAME_16.des());
	}

	@Override
	public void run() {
		try {
			log.trace("call {} with {}", handler, elem);
			handler.invoke(this);
		} catch (Exception e) {
			log.error("failed to call " + handler + " with " + elem, e);
		}
	}

	@Handler(type = FolderElement.class,
			title = "Umbenennen")
	private void deleteFolder() {
		try {
			FolderElement e = (FolderElement) elem;
			ProjectFolderDao dao = new ProjectFolderDao(App.getDb());
			InputDialog dialog = new InputDialog(UI.shell(), "Neuer Name",
					"Name des Ordners:", e.content.name, name -> {
						if (name == null || name.trim().length() == 0)
							return "Der Name darf nicht leer sein";
						boolean otherExists = dao.getAll().stream()
								.filter(f -> !Objects.equals(f, e.content)
										&& Strings.nullOrEqual(f.name, name))
								.findFirst()
								.isPresent();
						if (otherExists)
							return "Ein anderer Ordner mit dem Namen '" + name
									+ "' existiert bereits";
						return null;
					});
			if (dialog.open() != Window.OK)
				return;
			e.content.name = dialog.getValue();
			dao.update(e.content);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to rename folder", e);
		}
	}
}
