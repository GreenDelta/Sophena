package sophena.rcp.navigation.actions;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
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
		if (elem instanceof SubFolderElement) {
			SubFolderElement fe = (SubFolderElement) elem;
			if (fe.getType() == SubFolderType.PRODUCTION) {
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
