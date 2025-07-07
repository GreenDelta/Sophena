package sophena.rcp.editors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventBus {

	private final HashMap<String, List<Runnable>> clients = new HashMap<>();

	public void notify(String event) {
		List<Runnable> targets = clients.get(event);
		if (targets == null)
			return;
		for (Runnable target : targets) {
			target.run();
		}
	}

	public void on(List<String> events, Runnable target) {
		if (events == null || target == null)
			return;
		for (String event : events) {
			on(event, target);
		}
	}

	public void on(String event, Runnable target) {
		if (event == null || target == null)
			return;
		List<Runnable> list = clients.computeIfAbsent(event, k -> new ArrayList<>());
		for (Runnable t : list) {
			if (t == target)
				return;
		}
		list.add(target);
	}

}
