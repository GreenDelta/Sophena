package sophena.rcp.navigation.actions;

import java.util.List;

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
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.size() != 1)
			return false;
		var elem = elements.getFirst();
		if (elem instanceof ProducerElement prodElem) {
			project = prodElem.getProject();
			return true;
		}
		if (elem instanceof SubFolderElement subElem
			&& subElem.getType() == SubFolderType.PRODUCTION) {
			project = subElem.getProject();
			return true;
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
