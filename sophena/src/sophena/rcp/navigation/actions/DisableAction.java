package sophena.rcp.navigation.actions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ConsumerDao;
import sophena.db.daos.ProducerDao;
import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProducerElement;

public class DisableAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.isEmpty())
			return false;
		var element = elements.getFirst();
		if (element instanceof ConsumerElement) {
			ConsumerElement e = (ConsumerElement) element;
			updateUI(e.content.disabled);
			elem = e;
			return true;
		}
		if (element instanceof ProducerElement) {
			ProducerElement e = (ProducerElement) element;
			updateUI(e.content.disabled);
			elem = e;
			return true;
		}
		return false;
	}

	private void updateUI(boolean disabled) {
		if (disabled) {
			setText("Aktivieren");
			setImageDescriptor(Icon.ENABLED_16.des());
		} else {
			setImageDescriptor(Icon.DISABLED_16.des());
			setText("Deaktivieren");
		}
	}

	@Override
	public void run() {
		if (elem instanceof ConsumerElement)
			updateConsumer((ConsumerElement) elem);
		else if (elem instanceof ProducerElement)
			updateProducer((ProducerElement) elem);
	}

	private void updateConsumer(ConsumerElement e) {
		try {
			ConsumerDao dao = new ConsumerDao(App.getDb());
			Consumer c = dao.get(e.content.id);
			c.disabled = !c.disabled;
			dao.update(c);
			Navigator.refresh();
		} catch (Exception ex) {
			log.error("failed to disable/enable consumer", e);
		}
	}

	private void updateProducer(ProducerElement e) {
		try {
			ProducerDao dao = new ProducerDao(App.getDb());
			Producer p = dao.get(e.content.id);
			p.disabled = !p.disabled;
			dao.update(p);
			Navigator.refresh();
		} catch (Exception ex) {
			log.error("failed to disable/enable producer", e);
		}
	}
}
