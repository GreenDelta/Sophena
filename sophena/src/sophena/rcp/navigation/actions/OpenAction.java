package sophena.rcp.navigation.actions;

import sophena.model.Consumer;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.FacilityElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProjectElement;

public class OpenAction extends NavigationAction {

	private NavigationElement elem;

	public OpenAction() {
		setText(M.Open);
		setImageDescriptor(Images.OPEN_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof ProjectElement) {
			elem = element;
			return true;
		}
		if (element instanceof FacilityElement) {
			elem = element;
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		if (elem instanceof ProjectElement)
			openProject();
		if (elem instanceof FacilityElement)
			openFacility();
	}

	private void openProject() {
		ProjectElement e = (ProjectElement) elem;
		ProjectEditor.open(e.getProject());
	}

	private void openFacility() {
		FacilityElement e = (FacilityElement) elem;
		Object content = e.getContent();
		if (content instanceof Consumer) {
			ConsumerEditor.open(e.getProject(), (Consumer) content);
		}
	}

}
