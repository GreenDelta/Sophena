package sophena.model;

import java.util.ArrayList;
import java.util.List;

public class Project {

	private String name;
	private String description;
	private final List<Producer> producers = new ArrayList<>();
	private final List<Consumer> consumers = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

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

}
