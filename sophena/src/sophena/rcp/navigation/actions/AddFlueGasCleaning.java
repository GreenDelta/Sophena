package sophena.rcp.navigation.actions;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;

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
}
