package sophena.rcp.navigation;

import java.util.List;

import sophena.model.Descriptor;

public interface NavigationElement {

	List<NavigationElement> getChilds();

	NavigationElement getParent();

	Descriptor getDescriptor();

}
