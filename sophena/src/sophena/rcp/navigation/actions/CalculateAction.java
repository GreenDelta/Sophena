package sophena.rcp.navigation.actions;

import org.eclipse.jface.resource.ImageDescriptor;

import sophena.rcp.Icon;
import sophena.rcp.editors.results.single.ResultEditor;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;

public class CalculateAction extends NavigationAction {

	private FolderElement elem;

	@Override
	public boolean accept(NavigationElement element) {
		if (!(element instanceof FolderElement))
			return false;
		FolderElement e = (FolderElement) element;
		if (e.getType() != FolderType.RESULTS)
			return false;
		elem = e;
		return true;
	}

	@Override
	public String getText() {
		return "Berechnen";
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Icon.LOAD_PROFILE_16.des();
	}

	@Override
	public void run() {
		ResultEditor.open(elem.getProject());
	}
}
