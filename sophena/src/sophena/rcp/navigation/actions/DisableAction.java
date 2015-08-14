package sophena.rcp.navigation.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ConsumerDao;
import sophena.db.daos.ProducerDao;
import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProducerElement;

public class DisableAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof ConsumerElement) {
			ConsumerElement e = (ConsumerElement) element;
			updateUI(e.getDescriptor().isDisabled());
			elem = e;
			return true;
		}
		if (element instanceof ProducerElement) {
			ProducerElement e = (ProducerElement) element;
			updateUI(e.getDescriptor().isDisabled());
			elem = e;
			return true;
		}
		return false;
	}

	private void updateUI(boolean disabled) {
		if (disabled) {
			setText("Aktivieren");
			setImageDescriptor(Images.ENABLED_16.des());
		} else {
			setImageDescriptor(Images.DISABLED_16.des());
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
			Consumer c = dao.get(e.getDescriptor().id);
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
			Producer p = dao.get(e.getDescriptor().id);
			p.setDisabled(!p.isDisabled());
			dao.update(p);
			Navigator.refresh();
		} catch (Exception ex) {
			log.error("failed to disable/enable producer", e);
		}
	}
}
