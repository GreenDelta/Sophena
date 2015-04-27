package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.navigation.NavigationElement;

class Handlers {

	private Handlers() {
	}

	public static Method find(NavigationElement element, NavigationAction action) {
		if (element == null || action == null)
			return null;
		try {
			for (Method m : action.getClass().getDeclaredMethods()) {
				if (!m.isAnnotationPresent(Handler.class))
					continue;
				Handler h = m.getAnnotation(Handler.class);
				Class<?> c = h.type();
				if (c.equals(element.getClass())) {
					action.setText(h.title());
					return m;
				}
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(Handlers.class);
			log.error("failed to find handler function", e);
		}
		return null;
	}

}
