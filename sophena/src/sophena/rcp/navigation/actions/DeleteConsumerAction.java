package sophena.rcp.navigation.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.MsgBox;

public class DeleteConsumerAction extends NavigationAction {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private List<ConsumerElement> consumers;
	private ProjectDescriptor project;

	public DeleteConsumerAction() {
		setText("Lösche Wärmeabnehmer");
		setImageDescriptor(Icon.DELETE_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.isEmpty())
			return false;
		consumers = new ArrayList<>();
		project = null;
		for (var elem : elements) {
			if (!(elem instanceof ConsumerElement ce))
				return false;
			// all consumers must belong to the same project
			if (project == null) {
				project = ce.getProject();
			} else if (!Objects.equals(project, ce.getProject())) {
				return false;
			}
			consumers.add(ce);
		}
		return !consumers.isEmpty();
	}

	@Override
	public void run() {
		if (consumers == null || consumers.isEmpty() || project == null)
			return;

		String msg = consumers.size() == 1
				? "Soll der ausgewählte Abnehmer wirklich gelöscht werden?"
				: "Sollen die " + consumers.size()
				+ " ausgewählten Abnehmer wirklich gelöscht werden?";
		if (!MsgBox.ask("Abnehmer löschen?", msg))
			return;

		try {
			var dao = new ProjectDao(App.getDb());
			Project p = dao.get(project.id);
			if (p == null)
				return;

			for (var ce : consumers) {
				Consumer c = Util.find(p.consumers, ce.content);
				if (c != null) {
					p.consumers.remove(c);
					Editors.close(c.id);
				}
			}

			dao.update(p);
			Navigator.refresh();
		} catch (Exception e) {
			log.error("failed to delete consumers", e);
		}
	}
}
