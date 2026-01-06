package sophena.rcp.navigation.actions;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.wizards.ThermosImportWizard;

public class ThermosImportAction extends NavigationAction {

	private ProjectDescriptor project;

	public ThermosImportAction() {
		setText("Import aus BioHeating-Tool");
		setImageDescriptor(Icon.IMPORT_16.des());
	}

	@Override
	public boolean accept(NavigationElement elem) {
		if (elem instanceof ProjectElement projectElem) {
			project = projectElem.content;
			return true;
		}
		project = null;
		return false;
	}

	@Override
	public void run() {
		if (project != null) {
			ThermosImportWizard.open(project);
		}
	}
}
