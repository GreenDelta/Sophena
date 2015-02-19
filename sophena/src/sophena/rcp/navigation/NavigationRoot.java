package sophena.rcp.navigation;

import java.util.List;

import sophena.model.Descriptor;

public class NavigationRoot implements NavigationElement {

	@Override
	public List<NavigationElement> getChilds() {
		return null; // TODO: return database projects
	}

	@Override
	public NavigationElement getParent() {
		return null;
	}

	@Override
	public Descriptor getDescriptor() {
		return null;
	}

}
