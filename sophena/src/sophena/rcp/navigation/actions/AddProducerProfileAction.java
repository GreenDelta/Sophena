package sophena.rcp.navigation.actions;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.wizards.ProducerProfileWizard;

public class AddProducerProfileAction extends NavigationAction {

	private ProjectDescriptor project;

	public AddProducerProfileAction() {
		setText("Neuer Erzeugerlastgang");
		setImageDescriptor(Icon.LOAD_PROFILE_16.des());
	}

	@Override
	public boolean accept(NavigationElement elem) {
		if (elem instanceof ProducerElement) {
			project = ((ProducerElement) elem).getProject();
			return true;
		}
		if (elem instanceof FolderElement) {
			FolderElement fe = (FolderElement) elem;
			if (fe.getType() == FolderType.PRODUCTION) {
				project = fe.getProject();
				return true;
			}
		}
		project = null;
		return false;
	}

	@Override
	public void run() {
		if (project == null)
			return;
		ProducerProfileWizard.open(project);
	}
}
