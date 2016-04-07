package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ConsumerDao;
import sophena.db.daos.ProducerDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.RootEntity;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProducerElement;

public class DuplicateAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;
	private Method handler;

	public DuplicateAction() {
		setImageDescriptor(Icon.COPY_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
		handler = Handlers.find(element, this);
		if (handler == null) {
			elem = null;
			return false;
		} else {
			elem = element;
			return true;
		}
	}

	@Override
	public void run() {
		try {
			log.trace("call {} with {}", handler, elem);
			handler.invoke(this);
		} catch (Exception e) {
			log.error("failed to call " + handler + " with " + elem, e);
		}
	}

	@Handler(type = ConsumerElement.class, title = "Duplizieren")
	private void copyConsumer() {
		ConsumerElement e = (ConsumerElement) elem;
		ProjectDao pDao = new ProjectDao(App.getDb());
		Project project = pDao.get(e.getProject().id);
		ConsumerDao cDao = new ConsumerDao(App.getDb());
		Consumer consumer = cDao.get(e.getDescriptor().id);
		if (project == null || consumer == null)
			return;
		String name = getCopyName(consumer.name, project.consumers);
		Consumer clone = consumer.clone();
		clone.name = name;
		project.consumers.add(clone);
		pDao.update(project);
		Navigator.refresh();
	}

	@Handler(type = ProducerElement.class, title = "Duplizieren")
	private void copyProducer() {
		ProducerElement e = (ProducerElement) elem;
		ProjectDao projDao = new ProjectDao(App.getDb());
		Project project = projDao.get(e.getProject().id);
		ProducerDao prodDao = new ProducerDao(App.getDb());
		Producer producer = prodDao.get(e.getDescriptor().id);
		if (project == null || producer == null)
			return;
		String name = getCopyName(producer.name, project.producers);
		Producer clone = producer.clone();
		clone.name = name;
		project.producers.add(clone);
		projDao.update(project);
		Navigator.refresh();
	}

	private String getCopyName(String raw,
			List<? extends RootEntity> existing) {
		String baseName = getBaseName(raw);
		int maxCount = 0;
		for (RootEntity e : existing) {
			String b = getBaseName(e.name);
			if (!baseName.equalsIgnoreCase(b))
				continue;
			int count = getCount(e.name);
			maxCount = Math.max(maxCount, count);
		}
		maxCount++;
		return baseName + " (" + maxCount + ")";
	}

	private String getBaseName(String name) {
		if (name == null)
			return "";
		Pattern p = Pattern.compile("(.*)\\(([0-9]+)\\)");
		Matcher m = p.matcher(name);
		if (m.matches())
			return m.group(1).trim();
		else
			return name.trim();
	}

	private int getCount(String name) {
		if (name == null)
			return 0;
		Pattern p = Pattern.compile("(.*)\\(([0-9]+)\\)");
		Matcher m = p.matcher(name);
		if (!m.matches())
			return 0;
		String count = m.group(2);
		return Integer.parseInt(count);
	}

}
