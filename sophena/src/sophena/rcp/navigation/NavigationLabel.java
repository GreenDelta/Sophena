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

	private Font boldFont;
	private FontRegistry fontReg;

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
		fontReg = new FontRegistry();
		Display display = Display.getCurrent();
		if (display == null)
			return;
		Font sysFont = display.getSystemFont();
		boldFont = fontReg.getBold(sysFont.getFontData()[0].getName());
	}

	@Override
	public Font getFont(Object element) {
		if (element instanceof ProjectElement)
			return boldFont;
		if (element instanceof StructureElement)
			return boldFont;
		else
			return null;
	}

}
