package sophena.rcp.navigation.actions;

import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.StructureElement;
import sophena.rcp.wizards.ConsumerWizard;

public class NewAction extends NavigationAction {

	private StructureElement elem;

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof StructureElement) {
			elem = (StructureElement) element;
			return elem.getType() == StructureElement.USAGE;
		}
		return false;
	}

	@Override
	public String getText() {
		if (elem == null)
			return "#Neu";
		switch (elem.getType()) {
			case StructureElement.USAGE:
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
