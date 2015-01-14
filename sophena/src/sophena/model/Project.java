package sophena.model;

import java.util.ArrayList;
import java.util.List;

public class Project extends RootEntity {

	private String description;
	private final List<Producer> producers = new ArrayList<>();
	private final List<Consumer> consumers = new ArrayList<>();
	private final List<Pump> pumps = new ArrayList<>();
	private final List<Pipe> pipes = new ArrayList<>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Producer> getProducers() {
		return producers;
	}

	public List<Consumer> getConsumers() {
		return consumers;
	}

	public List<Pipe> getPipes() {
		return pipes;
	}

	public List<Pump> getPumps() {
		return pumps;
	}

	/**
	 * Returns a new list with all facilities (producers and consumers)
	 * contained.
	 */
	public List<Facility> getFacilities() {
		List<Facility> list = new ArrayList<>(producers.size()
				+ consumers.size());
		list.addAll(producers);
		list.addAll(consumers);
		list.addAll(pumps);
		return list;
	}
}
