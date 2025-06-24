package sophena.rcp.navigation.actions;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;

public class AddFlueGasCleaning extends NavigationAction {

	private ProjectDescriptor project;

	public AddFlueGasCleaning() {
		setText("Neue Rauchgasreinigung");
		setImageDescriptor(Icon.FLUE_GAS_16.des());
	}

	@Override
	public boolean accept(NavigationElement elem) {
		if (elem instanceof ProducerElement pe) {
			project = pe.getProject();
			return true;
		}
		if (elem instanceof SubFolderElement fe) {
			if (fe.getType() != SubFolderType.PRODUCTION)
				return false;
			project = fe.getProject();
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		Cleanings.add(project);
	}
}
