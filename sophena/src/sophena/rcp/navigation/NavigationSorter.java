package sophena.rcp.navigation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class NavigationSorter extends ViewerComparator {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return e1 instanceof NavigationElement n1
			&& e2 instanceof NavigationElement n2
			? n1.compareTo(n2)
			: super.compare(viewer, e1, e2);
	}

}
