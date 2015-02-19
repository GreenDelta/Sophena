package sophena.rcp.navigation;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import sophena.model.Descriptor;
import sophena.rcp.utils.Strings;

public class NavigationSorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (!(e1 instanceof NavigationElement)
				|| !(e2 instanceof NavigationElement))
			return super.compare(viewer, e1, e2);
		Descriptor d1 = ((NavigationElement) e1).getDescriptor();
		Descriptor d2 = ((NavigationElement) e2).getDescriptor();
		if (d1 == null || d2 == null)
			return 0;
		else
			return Strings.compare(d1.getName(), d2.getName());
	}

}
