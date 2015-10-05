package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import sophena.model.descriptors.ProjectDescriptor;

@Entity
@Table(name = "tbl_projects")
public class Project extends RootEntity {

	@Column(name = "project_duration")
	public int projectDuration;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	public final List<Producer> producers = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	public final List<Consumer> consumers = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_weather_station")
	public WeatherStation weatherStation;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_cost_settings")
	public CostSettings costSettings;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_heat_net")
	public HeatNet heatNet;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	public final List<ProductEntry> productEntries = new ArrayList<>();

	@Override
	public Project clone() {
		Project clone = new Project();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.projectDuration = projectDuration;
		clone.weatherStation = weatherStation;
		for (Consumer consumer : consumers)
			clone.consumers.add(consumer.clone());
		for (Producer producer : producers)
			clone.producers.add(producer.clone());
		if (heatNet != null)
			clone.heatNet = heatNet.clone();
		if (costSettings != null)
			clone.costSettings = costSettings.clone();
		return clone;
	}

	public ProjectDescriptor toDescriptor() {
		ProjectDescriptor d = new ProjectDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		return d;
	}
}
