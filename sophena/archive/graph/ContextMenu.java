package sophena.rcp.editors.graph;

import org.eclipse.gef.ContextMenuProvider;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.ui.actions.ActionRegistry;
import org.eclipse.gef.ui.actions.GEFActionConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.ui.actions.ActionFactory;

public class ContextMenu extends ContextMenuProvider {

	private ActionRegistry registry;

	public ContextMenu(EditPartViewer viewer, ActionRegistry registry) {
		super(viewer);
		this.registry = registry;
	}

	@Override
	public void buildContextMenu(IMenuManager menu) {
		GEFActionConstants.addStandardActionGroups(menu);

		IAction a = registry.getAction(GEFActionConstants.ZOOM_IN);
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, a);
		a = registry.getAction(GEFActionConstants.ZOOM_OUT);
		menu.appendToGroup(GEFActionConstants.GROUP_VIEW, a);
		a = registry.getAction(ActionFactory.DELETE.getId());
		menu.appendToGroup(GEFActionConstants.GROUP_EDIT, a);
	}
}
