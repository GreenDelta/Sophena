package sophena.rcp.navigation.actions;

import org.eclipse.jface.window.Window;

import sophena.db.daos.ProjectDao;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.wizards.FlueGasCleaningEntryWizard;

public class AddFlueGasCleaning extends NavigationAction {

	private ProjectDescriptor project;

	public AddFlueGasCleaning() {
		setText("Neue Rauchgasreinigung");
		setImageDescriptor(Icon.FLUE_GAS_16.des());
	}

	@Override
	public boolean accept(NavigationElement elem) {
		if (elem instanceof ProducerElement) {
			ProducerElement pe = (ProducerElement) elem;
			project = pe.getProject();
			return true;
		}
		if (elem instanceof FolderElement) {
			FolderElement fe = (FolderElement) elem;
			if (fe.getType() != FolderType.PRODUCTION)
				return false;
			project = fe.getProject();
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		if (project == null)
			return;
		ProjectDao dao = new ProjectDao(App.getDb());
		Project p = dao.get(project.id);
		FlueGasCleaningEntry entry = new FlueGasCleaningEntry();
		entry.costs = new ProductCosts();
		if (FlueGasCleaningEntryWizard.open(entry) != Window.OK)
			return;
		p.flueGasCleaningEntries.add(entry);
		dao.update(p);
		Navigator.refresh();
	}
}
