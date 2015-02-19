package sophena.rcp.navigation;

import java.util.List;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonContentProvider;

public class NavigationContent implements ICommonContentProvider {

	@Override
	public Object[] getElements(Object input) {
		return getChildren(input);
	}

	@Override
	public Object[] getChildren(Object parent) {
		if (!(parent instanceof NavigationElement))
			return new Object[0];
		NavigationElement e = (NavigationElement) parent;
		List<NavigationElement> childs = e.getChilds();
		if (childs == null)
			return new Object[0];
		else
			return childs.toArray();
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof NavigationElement)
			return ((NavigationElement) element).getParent();
		else
			return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (!(element instanceof NavigationElement))
			return false;
		NavigationElement e = (NavigationElement) element;
		return !e.getChilds().isEmpty();
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void restoreState(IMemento aMemento) {
	}

	@Override
	public void saveState(IMemento aMemento) {
	}

	@Override
	public void init(ICommonContentExtensionSite aConfig) {
	}

}
