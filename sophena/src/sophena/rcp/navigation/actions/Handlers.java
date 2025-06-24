package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;

import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.SubFolderElement;

class Handlers {

	private Handlers() {
	}

	static Method find(NavigationElement element, Action action) {
		if (element == null || action == null)
			return null;
		try {
			for (Method m : action.getClass().getDeclaredMethods()) {
				if (!m.isAnnotationPresent(Handler.class))
					continue;
				Handler h = m.getAnnotation(Handler.class);
				if (match(h, element)) {
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

	private static boolean match(Handler h, NavigationElement e) {
		if (h == null || e == null)
			return false;
		Class<?> c = h.type();
		if (!c.equals(e.getClass()))
			return false;
		if (!(e instanceof SubFolderElement fe))
			return true;
		return fe.getType() == h.folderType();
	}

}
