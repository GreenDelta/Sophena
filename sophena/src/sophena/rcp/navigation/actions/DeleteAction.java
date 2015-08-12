package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.RootEntity;
import sophena.model.descriptors.Descriptor;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.navigation.ConsumerElement;
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
		setImageDescriptor(Images.DELETE_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
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

	@Handler(type = ProjectElement.class, title = "Lösche Projekt")
	private void deleteProject() {
		boolean del = MsgBox.ask("Projekt löschen?",
				"Soll das ausgewählte Projekt wirklich gelöscht werden?");
		if (!del)
			return;
		try {
			ProjectElement e = (ProjectElement) elem;
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(e.getDescriptor().id);
			dao.delete(p);
			Editors.close(p.id);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete project", e);
		}
	}

	@Handler(type = ConsumerElement.class, title = "Lösche Wärmeabnehmer")
	private void deleteConsumer() {
		boolean del = MsgBox.ask("Abnehmer löschen?",
				"Soll der ausgewählte Abnehmer wirklich gelöscht werden?");
		if (!del)
			return;
		try {
			ConsumerElement e = (ConsumerElement) elem;
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(e.getProject().id);
			if (p == null)
				return;
			Consumer c = find(p.getConsumers(), e.getDescriptor());
			if (c == null)
				return;
			p.getConsumers().remove(c);
			dao.update(p);
			Editors.close(c.id);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete consumer", e);
		}
	}

	@Handler(type = ProducerElement.class, title = "Lösche Wärmeerzeuger")
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
			Producer prod = find(p.getProducers(), e.getDescriptor());
			if (prod == null)
				return;
			p.getProducers().remove(prod);
			dao.update(p);
			Editors.close(prod.id);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete producer", e);
		}
	}

	private <T extends RootEntity> T find(List<T> list, Descriptor d) {
		if (list == null || d == null)
			return null;
		for (T e : list) {
			if (Objects.equals(e.id, d.id))
				return e;
		}
		return null;
	}

}
