package sophena.rcp.navigation;

import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
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
			return ((NavigationElement) element).getLabel();
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

	@Override
	public Font getFont(Object element) {
		FontRegistry registry = new FontRegistry();
		return registry.getBold(Display.getCurrent().getSystemFont()
				.getFontData()[0].getName());

	}

}
