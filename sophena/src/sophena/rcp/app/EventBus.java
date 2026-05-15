package sophena.rcp.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventBus {

	private final List<Listener> listeners = new ArrayList<>();

	public void emit(Object event) {
		if (event == null) return;
		for (var listener : listeners) {
			if (matches(event, listener.object)) {
				listener.fn.run();
			}
		}
	}

	/// Subscribes for events linked to the given object. If the provided object
	/// is a class, then the given function is also executed for every event object
	/// that is an instance of that class.
	public void subscribe(Object object, Runnable fn) {
		if (object == null || fn == null)
			return;
		listeners.add(new Listener(object, fn));
	}

	public void unsubscribe(Runnable fn) {
		listeners.removeIf(l -> Objects.equals(l.fn, fn));
	}

	private boolean matches(Object eventObj, Object listenerObj) {
		if (Objects.equals(eventObj, listenerObj))
			return true;
		return listenerObj instanceof Class<?> cls && cls.isInstance(eventObj);
	}

	private record Listener(Object object, Runnable fn) {
	}
}
