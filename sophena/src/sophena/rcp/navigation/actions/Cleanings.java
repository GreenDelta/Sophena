package sophena.rcp.navigation.actions;

import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.descriptors.CleaningDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.navigation.CleaningElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.wizards.FlueGasCleaningEntryWizard;

class Cleanings {

	private Cleanings() {
	}

	static void add(ProjectDescriptor project) {
		if (project == null)
			return;
		ProjectDao dao = new ProjectDao(App.getDb());
		Project p = dao.get(project.id);
		FlueGasCleaningEntry entry = new FlueGasCleaningEntry();
		entry.id = UUID.randomUUID().toString();
		entry.costs = new ProductCosts();
		if (FlueGasCleaningEntryWizard.open(entry) != Window.OK)
			return;
		p.flueGasCleaningEntries.add(entry);
		doUpdate(dao, p);
	}

	static void delete(CleaningElement e) {
		Data d = Data.load(e);
		if (d == null)
			return;
		boolean del = MsgBox.ask("Rauchgasreinigung löschen?",
				"Soll die ausgewählte Rauchgasreinigung wirklich gelöscht werden?");
		if (!del)
			return;
		d.project.flueGasCleaningEntries.remove(d.entry);
		doUpdate(d.dao, d.project);
	}

	static void open(CleaningElement e) {
		Data d = Data.load(e);
		if (d == null)
			return;
		FlueGasCleaningEntry clone = d.entry.copy();
		if (FlueGasCleaningEntryWizard.open(clone) != Window.OK)
			return;
		d.entry.product = clone.product;
		if (clone.costs != null) {
			d.entry.costs = clone.costs.copy();
		} else {
			d.entry.costs = null;
		}
		doUpdate(d.dao, d.project);
	}

	private static void doUpdate(ProjectDao dao, Project project) {
		try {
			dao.update(project);
			Navigator.refresh();
		} catch (Exception ex) {
			Logger log = LoggerFactory.getLogger(Cleanings.class);
			log.error("failed to delete flue gas cleaning", ex);
		}
	}

	private static class Data {
		ProjectDao dao;
		Project project;
		FlueGasCleaningEntry entry;

		static Data load(CleaningElement e) {
			if (e == null)
				return null;
			CleaningDescriptor d = e.content;
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(e.getProject().id);
			if (p == null || d == null)
				return null;
			FlueGasCleaningEntry entry = Util.find(p.flueGasCleaningEntries, d);
			if (entry == null)
				return null;
			Data data = new Data();
			data.dao = dao;
			data.project = p;
			data.entry = entry;
			return data;
		}
	}

}
