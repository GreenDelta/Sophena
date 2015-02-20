package sophena.rcp.navigation.actions;

import sophena.rcp.Images;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProjectElement;

public class OpenAction extends NavigationAction {

	private NavigationElement elem;

	public OpenAction() {
		setText("#Öffnen");
		setImageDescriptor(Images.OPEN_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof ProjectElement) {
			elem = element;
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		if (elem instanceof ProjectElement)
			openProject();
	}

	private void openProject() {
		ProjectElement e = (ProjectElement) elem;
		ProjectEditor.open(e.getProject());
	}

}
