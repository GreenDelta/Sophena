package sophena.rcp.navigation.actions;

import java.util.List;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.wizards.BiogasPlantProducerWizard;

public class AddBiogasPlantAction extends NavigationAction {

	private ProjectDescriptor project;

	public AddBiogasPlantAction() {
		setText("Biogasanlage hinzufügen");
		setImageDescriptor(Icon.BIOGAS_SUBSTRATE_16.des());
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
		if (project != null) {
			BiogasPlantProducerWizard.open(project);
		}
	}
}
