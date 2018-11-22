package sophena.rcp.navigation.actions;

import java.util.Optional;

import org.eclipse.jface.resource.ImageDescriptor;

import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.Icon;
import sophena.rcp.editors.results.compare.ComparisonDialog;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProjectElement;

public class CompareAction extends NavigationAction {

	private ProjectElement elem;

	@Override
	public boolean accept(NavigationElement e) {
		if (!(e instanceof ProjectElement))
			return false;
		elem = (ProjectElement) e;
		return elem.content != null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Icon.BAR_CHART_16.des();
	}

	@Override
	public String getText() {
		return "Vergleiche mit...";
	}

	@Override
	public void run() {
		if (elem == null)
			return;
		ProjectDescriptor d = elem.content;
		ComparisonDialog.open(Optional.of(d));
	}

}
