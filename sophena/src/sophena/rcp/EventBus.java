package sophena.rcp;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventBus {

	private final List<Listener> listeners = new ArrayList<>();

	public void emit(Object event) {
		if (event == null) return;
		for (var listener : listeners) {
			if (Objects.equals(event, listener.event)) {
				listener.fn.run();
			}
		}
	}

	public void subscribe(Object event, Runnable fn) {
		if (event == null || fn == null) return;
		listeners.add(new Listener(event, fn));
	}

	public void unsubscribe(Runnable fn) {
		listeners.removeIf(l -> Objects.equals(l.fn, fn));
	}

	private record Listener(Object event, Runnable fn) {
	}
}
