package sophena.rcp.navigation;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonActionProvider;

import sophena.rcp.navigation.actions.AddAction;
import sophena.rcp.navigation.actions.AddConsumerProfileAction;
import sophena.rcp.navigation.actions.AddFlueGasCleaning;
import sophena.rcp.navigation.actions.AddProducerProfileAction;
import sophena.rcp.navigation.actions.CalculateAction;
import sophena.rcp.navigation.actions.CompareAction;
import sophena.rcp.navigation.actions.ConsumerImportAction;
import sophena.rcp.navigation.actions.DeleteAction;
import sophena.rcp.navigation.actions.DisableAction;
import sophena.rcp.navigation.actions.DuplicateAction;
import sophena.rcp.navigation.actions.ExportAction;
import sophena.rcp.navigation.actions.NavigationAction;
import sophena.rcp.navigation.actions.NewFolderAction;
import sophena.rcp.navigation.actions.NewProjectAction;
import sophena.rcp.navigation.actions.OpenAction;
import sophena.rcp.navigation.actions.RenameAction;
import sophena.rcp.navigation.actions.SaveAsAction;
import sophena.rcp.utils.Viewers;

public class NavigationMenu extends CommonActionProvider {

	private final NavigationAction[][] actionGroups = {
			{
					new OpenAction(),
					new CompareAction(),
					new AddAction(),
					new AddConsumerProfileAction(),
					new ConsumerImportAction(),
					new AddProducerProfileAction(),
					new AddFlueGasCleaning(),
					new DuplicateAction(),
					new RenameAction(),
					new DisableAction(),
					new SaveAsAction(),
					new DeleteAction(),
					new ExportAction()
			},
			{
					new CalculateAction()
			}
	};

	@Override
	public void fillContextMenu(IMenuManager menu) {
		var con = getContext();
		var selection = (IStructuredSelection) con.getSelection();
		if (selection == null || selection.isEmpty()) {
			menu.add(new NewProjectAction());
			menu.add(new NewFolderAction());
			return;
		}
		NavigationElement element = Viewers.getFirst(selection);
		for (var group : actionGroups) {
			int count = 0;
			for (var action : group) {
				if (!action.accept(element))
					continue;
				menu.add(action);
				count++;
			}
			if (count > 0) {
				menu.add(new Separator());
			}
		}
		menu.add(new NewProjectAction());
		menu.add(new NewFolderAction());
	}

}
