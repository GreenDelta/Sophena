package sophena.rcp.navigation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

public class NavigationSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof NavigationElement)
				|| !(e2 instanceof NavigationElement))
			return super.compare(viewer, e1, e2);
		NavigationElement n1 = (NavigationElement) e1;
		NavigationElement n2 = (NavigationElement) e2;
		return n1.compareTo(n2);
	}

}
