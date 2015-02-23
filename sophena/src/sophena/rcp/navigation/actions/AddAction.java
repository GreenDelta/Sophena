package sophena.rcp.navigation.actions;

import org.eclipse.jface.resource.ImageDescriptor;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.StructureElement;
import sophena.rcp.wizards.ConsumerWizard;
import sophena.rcp.wizards.ProducerWizard;

public class AddAction extends NavigationAction {

	private StructureElement elem;

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof StructureElement) {
			elem = (StructureElement) element;
			setText(getText(elem.getType()));
			return elem.getType() == StructureElement.CONSUMPTION
					|| elem.getType() == StructureElement.PRODUCTION;
		}
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Images.ADD_16.des();
	}

	private String getText(int type) {
		switch (type) {
			case StructureElement.CONSUMPTION:
				return M.NewConsumer;
			case StructureElement.PRODUCTION:
				return M.NewProducer;
			default:
				return "#Neu";
		}
	}

	@Override
	public void run() {
		switch (elem.getType()) {
			case StructureElement.PRODUCTION:
				ProducerWizard.open(elem.getProject());
				break;
			case StructureElement.CONSUMPTION:
				ConsumerWizard.open(elem.getProject());
				break;
		}
	}
}
