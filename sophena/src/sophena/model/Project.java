package sophena.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tbl_projects")
public class Project extends RootEntity {

	@Transient
	private final List<Producer> producers = new ArrayList<>();

	@Transient
	private final List<Consumer> consumers = new ArrayList<>();

	@Transient
	private final List<Pump> pumps = new ArrayList<>();

	@Transient
	private final List<Pipe> pipes = new ArrayList<>();

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
