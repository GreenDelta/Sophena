package sophena.rcp.navigation.actions;

import org.eclipse.jface.resource.ImageDescriptor;

import sophena.rcp.Images;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.StructureElement;
import sophena.rcp.wizards.ConsumerWizard;

public class AddAction extends NavigationAction {

	private StructureElement elem;

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof StructureElement) {
			elem = (StructureElement) element;
			return elem.getType() == StructureElement.CONSUMPTION;
		}
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Images.ADD_16.des();
	}

	@Override
	public String getText() {
		if (elem == null)
			return "#Neu";
		switch (elem.getType()) {
		case StructureElement.CONSUMPTION:
			return "#Neuer Wärmeabnehmer";
		default:
			return "#Neu";
		}
	}

	@Override
	public void run() {
		ConsumerWizard.open(elem.getProject());
	}
}
