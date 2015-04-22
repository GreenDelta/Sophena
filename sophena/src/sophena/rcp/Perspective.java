package sophena.rcp;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IViewLayout;

public class Perspective implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
		IFolderLayout naviLayout = layout.createFolder("Navigation",
				IPageLayout.LEFT, 0.31f, editorArea);
		naviLayout.addView("sophena.Navigator");
		IViewLayout vLayout = layout.getViewLayout("sophena.Navigator");
		vLayout.setCloseable(false);
		vLayout.setMoveable(false);
	}
}
