package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;

import sophena.model.Project;

public class ProjectElement implements NavigationElement {

	private Project project;

	public ProjectElement(Project project) {
		this.project = project;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public NavigationElement getParent() {
		return null;
	}

}
