package sophena.rcp.navigation.actions;

import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;

import sophena.rcp.Icon;
import sophena.rcp.editors.results.single.ResultEditor;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;

public class CalculateAction extends NavigationAction {

	private SubFolderElement elem;

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.size() != 1)
			return false;
		var element = elements.getFirst();
		if (!(element instanceof SubFolderElement))
			return false;
		SubFolderElement e = (SubFolderElement) element;
		if (e.getType() != SubFolderType.RESULTS)
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
