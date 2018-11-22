package sophena.rcp.navigation.actions;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.wizards.ConsumerProfileWizard;

public class AddConsumerProfileAction extends NavigationAction {

	private ProjectDescriptor project;

	public AddConsumerProfileAction() {
		setText("Neuer Lastgang");
		setImageDescriptor(Icon.LOAD_PROFILE_16.des());
	}

	@Override
	public boolean accept(NavigationElement elem) {
		if (elem instanceof ConsumerElement) {
			project = ((ConsumerElement) elem).getProject();
			return true;
		}
		if (elem instanceof SubFolderElement) {
			SubFolderElement fe = (SubFolderElement) elem;
			if (fe.getType() == SubFolderType.CONSUMPTION) {
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
		ConsumerProfileWizard.open(project);
	}
}
