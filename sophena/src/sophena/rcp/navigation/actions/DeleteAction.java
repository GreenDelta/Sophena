package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.db.daos.ProjectFolderDao;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.navigation.CleaningElement;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.MsgBox;

public class DeleteAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;
	private Method handler;

	public DeleteAction() {
		setText(M.Delete);
		setImageDescriptor(Icon.DELETE_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.size() != 1)
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

	@Override
	public void run() {
		try {
			log.trace("call {} with {}", handler, elem);
			handler.invoke(this);
		} catch (Exception e) {
			log.error("failed to call " + handler + " with " + elem, e);
		}
	}

	@Handler(type = ProjectElement.class,
			title = "Lösche Projekt")
	private void deleteProject() {
		boolean del = MsgBox.ask("Projekt löschen?",
				"Soll das ausgewählte Projekt wirklich gelöscht werden?");
		if (!del)
			return;
		try {
			ProjectElement e = (ProjectElement) elem;
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(e.content.id);
			dao.delete(p);
			Editors.close(p.id);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete project", e);
		}
	}

	@Handler(type = FolderElement.class,
			title = "Lösche Ordner")
	private void deleteFolder() {
		try {
			FolderElement e = (FolderElement) elem;
			ProjectFolderDao dao = new ProjectFolderDao(App.getDb());
			if (!dao.getProjects(e.content).isEmpty()) {
				MsgBox.error("Ordner ist nicht leer",
						"Der ausgewählte Ordner ist nicht leer "
								+ "und kann daher nicht gelöscht werden.");
				return;
			}
			dao.delete(e.content);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete folder", e);
		}
	}

	@Handler(type = ProducerElement.class,
			title = "Lösche Wärmeerzeuger")
	private void deleteProducer() {
		boolean del = MsgBox.ask("Erzeuger löschen?",
				"Soll der ausgewählte Erzeuger wirklich gelöscht werden?");
		if (!del)
			return;
		try {
			ProducerElement e = (ProducerElement) elem;
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(e.getProject().id);
			if (p == null)
				return;
			Producer prod = Util.find(p.producers, e.content);
			if (prod == null)
				return;
			p.producers.remove(prod);
			dao.update(p);
			Editors.close(prod.id);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete producer", e);
		}
	}

	@Handler(type = CleaningElement.class,
			title = "Lösche Rauchgasreinigung")
	private void deleteCleaning() {
		CleaningElement e = (CleaningElement) elem;
		Cleanings.delete(e);
	}

}
