package sophena.rcp.navigation;

import java.util.List;

public interface NavigationElement {

	List<NavigationElement> getChilds();

	NavigationElement getParent();

	String getLabel();

	int compareTo(NavigationElement other);

}
