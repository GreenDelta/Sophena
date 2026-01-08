package sophena.rcp.navigation.actions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.MsgBox;

public class DeleteAllConsumersAction extends NavigationAction {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private SubFolderElement folder;

	public DeleteAllConsumersAction() {
		setText("Lösche alle Wärmeabnehmer");
		setImageDescriptor(Icon.DELETE_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.size() != 1) {
			return false;
		}
		var elem = elements.get(0);
		if (elem instanceof SubFolderElement sf && sf.getType() == SubFolderType.CONSUMPTION) {
			folder = sf;
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		if (folder == null || folder.getProject() == null) {
			return;
		}

		if (!MsgBox.ask("Alle Abnehmer löschen?", "Sollen wirklich alle Wärmeabnehmer gelöscht werden?")) {
			return;
		}

		try {
			var dao = new ProjectDao(App.getDb());
			Project p = dao.get(folder.getProject().id);
			if (p == null) {
				return;
			}

			for (var c : p.consumers) {
				Editors.close(c.id);
			}
			p.consumers.clear();

			dao.update(p);
			Navigator.refresh();
		} catch (Exception e) {
			log.error("failed to delete all consumers", e);
		}
	}
}
