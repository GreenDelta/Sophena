package sophena.rcp.navigation;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.navigator.CommonActionProvider;

import sophena.rcp.navigation.actions.*;
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
