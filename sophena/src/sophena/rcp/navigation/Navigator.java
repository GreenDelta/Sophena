package sophena.rcp.navigation;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;

import sophena.model.Consumer;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.utils.Viewers;

public class Navigator extends CommonNavigator {

	private static final String ID = "sophena.Navigator";

	private NavigationRoot root;

	@Override
	protected Object getInitialInput() {
		root = new NavigationRoot();
		return root;
	}

	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		CommonViewer viewer = super.createCommonViewerObject(aParent);
		viewer.setUseHashlookup(true);
		return viewer;
	}

	@Override
	protected void initListeners(TreeViewer viewer) {
		super.initListeners(viewer);
		viewer.addDoubleClickListener((e) -> {
			NavigationElement elem = Viewers.getFirstSelected(viewer);
			if (elem instanceof ProjectElement) {
				ProjectElement pElem = (ProjectElement) elem;
				ProjectEditor.open(pElem.getProject());
			} else if (elem instanceof FacilityElement) {
				FacilityElement fElem = (FacilityElement) elem;
				Object content = fElem.getContent();
				if (content instanceof Consumer)
					ConsumerEditor.open(fElem.getProject(), (Consumer) content);
			}
		});
	}

	public NavigationRoot getRoot() {
		return root;
	}

	public static void refresh(Object content) {
		NavigationElement elem = findElement(content);
		CommonViewer viewer = getNavigationViewer();
		if (elem == null || viewer == null)
			return;
		elem.update();
		Object[] oldExpansion = viewer.getExpandedElements();
		viewer.refresh(); // TODO: does not work currently with element
		if (oldExpansion == null)
			return;
		setRefreshedExpansion(viewer, oldExpansion);
	}

	private static NavigationElement findElement(Object content) {
		NavigationRoot root = getNavigationRoot();
		if (content == null || root == null)
			return null;
		Queue<NavigationElement> queue = new ArrayDeque<>();
		queue.add(root);
		while (!queue.isEmpty()) {
			NavigationElement next = queue.poll();
			if (Objects.equals(next.getContent(), content))
				return next;
			queue.addAll(next.getChilds());
		}
		return null;
	}

	private static void setRefreshedExpansion(CommonViewer viewer,
			Object[] oldExpansion) {
		List<NavigationElement> newExpanded = new ArrayList<>();
		for (Object expandedElem : oldExpansion) {
			if (!(expandedElem instanceof NavigationElement))
				continue;
			NavigationElement oldElem = (NavigationElement) expandedElem;
			NavigationElement newElem = findElement(oldElem.getContent());
			if (newElem != null)
				newExpanded.add(newElem);
		}
		viewer.setExpandedElements(newExpanded.toArray());
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
