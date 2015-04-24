package sophena.rcp.navigation;

import java.util.List;

import org.eclipse.swt.graphics.Image;

public interface NavigationElement {

	List<NavigationElement> getChilds();

	NavigationElement getParent();

	int compareTo(NavigationElement other);

	String getLabel();

	Image getImage();

	void update();
}
