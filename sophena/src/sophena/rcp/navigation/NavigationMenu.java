package sophena.rcp.navigation;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.navigator.CommonActionProvider;

import sophena.rcp.navigation.actions.AddAction;
import sophena.rcp.navigation.actions.CalculateAction;
import sophena.rcp.navigation.actions.DeleteAction;
import sophena.rcp.navigation.actions.NavigationAction;
import sophena.rcp.navigation.actions.NewProjectAction;
import sophena.rcp.navigation.actions.NewVariantAction;
import sophena.rcp.navigation.actions.OpenAction;
import sophena.rcp.utils.Viewers;

public class NavigationMenu extends CommonActionProvider {

	private NavigationAction[] menuActions = {
			new AddAction(),
			new OpenAction(),
			new NewVariantAction(),
			new DeleteAction(),
			new CalculateAction()
	};

	@Override
	public void fillContextMenu(IMenuManager menu) {
		ActionContext con = getContext();
		IStructuredSelection selection = (IStructuredSelection) con
				.getSelection();
		if (selection == null || selection.isEmpty()) {
			menu.add(new NewProjectAction());
			return;
		}
		NavigationElement element = Viewers.getFirst(selection);
		for (NavigationAction action : menuActions) {
			if (action.accept(element))
				menu.add(action);
		}
	}

}
