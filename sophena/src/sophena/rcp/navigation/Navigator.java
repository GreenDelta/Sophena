package sophena.rcp.navigation;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

public class Navigator extends CommonNavigator {

	private static final String ID = "sophena.Navigator";

	private NavigationRoot root;

	@Override
	protected Object getInitialInput() {
		root = new NavigationRoot();
		return root;
	}

	public NavigationRoot getRoot() {
		return root;
	}

	public static void refresh() {
		CommonViewer viewer = getNavigationViewer();
		NavigationRoot root = getNavigationRoot();
		if (viewer != null && root != null) {
			root.update();
			viewer.refresh();
			viewer.expandToLevel(2);
		}
	}

	private static CommonViewer getNavigationViewer() {
		CommonViewer viewer = null;
		Navigator instance = getInstance();
		if (instance != null) {
			viewer = instance.getCommonViewer();
		}
		return viewer;
	}

	private static NavigationRoot getNavigationRoot() {
		NavigationRoot root = null;
		Navigator navigator = getInstance();
		if (navigator != null)
			root = navigator.getRoot();
		return root;
	}

	private static Navigator getInstance() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench == null)
			return null;
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		if (window == null)
			return null;
		IWorkbenchPage page = window.getActivePage();
		if (page == null)
			return null;
		IViewPart part = page.findView(ID);
		if (part instanceof Navigator)
			return (Navigator) part;
		return null;
	}

}
