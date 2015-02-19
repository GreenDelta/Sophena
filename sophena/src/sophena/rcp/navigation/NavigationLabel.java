package sophena.rcp.navigation;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.navigator.ICommonContentExtensionSite;
import org.eclipse.ui.navigator.ICommonLabelProvider;

import sophena.model.Descriptor;

public class NavigationLabel extends ColumnLabelProvider implements
		ICommonLabelProvider {

	@Override
	public String getText(Object element) {
		if (!(element instanceof NavigationElement))
			return super.getText(element);
		Descriptor d = ((NavigationElement) element).getDescriptor();
		if (d == null)
			return super.getText(element);
		else
			return d.getName();
	}

	@Override
	public String getDescription(Object element) {
		if (!(element instanceof NavigationElement))
			return null;
		Descriptor d = ((NavigationElement) element).getDescriptor();
		return d == null ? null : d.getDescription();
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
