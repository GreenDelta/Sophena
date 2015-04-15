package sophena.rcp.navigation.actions;

import org.eclipse.jface.resource.ImageDescriptor;

import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.wizards.ConsumerWizard;
import sophena.rcp.wizards.ProducerWizard;

public class AddAction extends NavigationAction {

	private FolderElement elem;

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof FolderElement) {
			elem = (FolderElement) element;
			setText(getText(elem.getType()));
			return elem.getType() == FolderType.CONSUMPTION
					|| elem.getType() == FolderType.PRODUCTION;
		}
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Images.ADD_16.des();
	}

	private String getText(FolderType type) {
		switch (type) {
		case CONSUMPTION:
			return M.NewConsumer;
		case PRODUCTION:
			return M.NewProducer;
		default:
			return "#Neu";
		}
	}

	@Override
	public void run() {
		switch (elem.getType()) {
		case PRODUCTION:
			ProducerWizard.open(elem.getProject());
			break;
		case CONSUMPTION:
			ConsumerWizard.open(elem.getProject());
			break;
		}
	}
}
