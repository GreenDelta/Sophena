package sophena.rcp.navigation;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;

import sophena.rcp.navigation.actions.DeleteAction;
import sophena.rcp.navigation.actions.NavigationAction;
import sophena.rcp.navigation.actions.OpenAction;
import sophena.rcp.utils.Viewers;

public class NavigationMenu extends CommonActionProvider {

	private NavigationAction[] menuActions = {
			new OpenAction(),
			new DeleteAction()
	};

	@Override
	public void fillContextMenu(IMenuManager menu) {
		ActionContext con = getContext();
		IStructuredSelection selection = (IStructuredSelection) con
				.getSelection();
		NavigationElement element = Viewers.getFirst(selection);
		for (NavigationAction action : menuActions) {
			if (action.accept(element))
				menu.add(action);
		}
	}

}
