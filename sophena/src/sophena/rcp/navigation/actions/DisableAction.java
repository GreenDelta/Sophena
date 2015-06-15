package sophena.rcp.navigation.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;

public class DisableAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof ConsumerElement) {
			ConsumerElement e = (ConsumerElement) element;
			updateText(e.getDescriptor().isDisabled());
			elem = e;
			return true;
		}
		if (element instanceof ProducerElement) {
			ProducerElement e = (ProducerElement) element;
			updateText(e.getDescriptor().isDisabled());
			elem = e;
			return true;
		}
		return false;
	}

	private void updateText(boolean disabled) {
		if (disabled)
			setText("Aktivieren");
		else
			setText("Deaktivieren");
	}

	@Override
	public void run() {
		// TODO: write disable function

	}

}
