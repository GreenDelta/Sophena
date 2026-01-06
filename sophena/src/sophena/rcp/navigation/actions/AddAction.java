package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.Icon;
import sophena.rcp.navigation.CleaningElement;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.wizards.ConsumerWizard;
import sophena.rcp.wizards.ProducerWizard;

public class AddAction extends NavigationAction {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;
	private Method handler;

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.isEmpty())
			return false;
		var e = elements.getFirst();
		handler = Handlers.find(e, this);
		if (handler == null) {
			elem = null;
			return false;
		} else {
			elem = e;
			return true;
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Icon.ADD_16.des();
	}

	@Override
	public void run() {
		try {
			log.trace("call {} with {}", handler, elem);
			handler.invoke(this);
		} catch (Exception e) {
			log.error("failed to call {} with {}", handler, elem, e);
		}
	}

	@Handler(type = SubFolderElement.class,
			title = "Neuer Wärmeerzeuger",
			folderType = SubFolderType.PRODUCTION)
	private void producerOnFolder() {
		SubFolderElement e = (SubFolderElement) elem;
		ProducerWizard.open(e.getProject());
	}

	@Handler(type = ProducerElement.class,
			title = "Neuer Wärmeerzeuger")
	private void producerOnElement() {
		ProducerElement e = (ProducerElement) elem;
		ProducerWizard.open(e.getProject());
	}

	@Handler(type = SubFolderElement.class,
			title = "Neuer Wärmeabnehmer",
			folderType = SubFolderType.CONSUMPTION)
	private void consumerOnFolder() {
		SubFolderElement e = (SubFolderElement) elem;
		ConsumerWizard.open(e.getProject());
	}

	@Handler(type = ConsumerElement.class,
			title = "Neuer Wärmeabnehmer")
	private void consumerOnElement() {
		ConsumerElement e = (ConsumerElement) elem;
		ConsumerWizard.open(e.getProject());
	}

	@Handler(type = CleaningElement.class,
			title = "Neue Rauchgasreinigung")
	private void cleaningOnElement() {
		CleaningElement e = (CleaningElement) elem;
		Cleanings.add(e.getProject());
	}

}
