package sophena.rcp.navigation;

import java.util.List;

import org.eclipse.swt.graphics.Image;

public interface NavigationElement {

	List<NavigationElement> getChilds();

	NavigationElement getParent();

	String getLabel();

	int compareTo(NavigationElement other);

	Object getContent();

	void update();

	Image getImage();

}
