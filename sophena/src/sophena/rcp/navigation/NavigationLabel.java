package sophena.rcp.navigation;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

public class NavigationLabel extends ColumnLabelProvider implements
		ICommonLabelProvider {

	@Override
	public String getText(Object element) {
		if (!(element instanceof NavigationElement))
			return super.getText(element);
		else
			return element.toString();
	}

	@Override
	public String getDescription(Object element) {
		return null;
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
