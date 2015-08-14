package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
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
	private int projectDuration;

	@Column(name = "is_variant")
	private boolean variant;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	private final List<Producer> producers = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	private final List<Consumer> consumers = new ArrayList<>();

	@JoinColumn(name = "f_project")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Project> variants = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_weather_station")
	private WeatherStation weatherStation;

	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_cost_settings")
	private CostSettings costSettings;

	@Embedded
	private HeatNet heatNet = new HeatNet();

	public List<Producer> getProducers() {
		return producers;
	}

	public List<Consumer> getConsumers() {
		return consumers;
	}

	public int getProjectDuration() {
		return projectDuration;
	}

	public void setProjectDuration(int projectDuration) {
		this.projectDuration = projectDuration;
	}

	public List<Project> getVariants() {
		return variants;
	}

	public boolean isVariant() {
		return variant;
	}

	public void setVariant(boolean variant) {
		this.variant = variant;
	}

	public WeatherStation getWeatherStation() {
		return weatherStation;
	}

	public void setWeatherStation(WeatherStation weatherStation) {
		this.weatherStation = weatherStation;
	}

	public HeatNet getHeatNet() {
		return heatNet;
	}

	public void setHeatNet(HeatNet heatNet) {
		this.heatNet = heatNet;
	}

	public CostSettings getCostSettings() {
		return costSettings;
	}

	public void setCostSettings(CostSettings costSettings) {
		this.costSettings = costSettings;
	}

	@Override
	public Project clone() {
		Project clone = new Project();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.setProjectDuration(getProjectDuration());
		clone.setWeatherStation(getWeatherStation());
		clone.setVariant(isVariant());
		for (Consumer consumer : getConsumers())
			clone.getConsumers().add(consumer.clone());
		for (Producer producer : getProducers())
			clone.getProducers().add(producer.clone());
		for (Project variant : getVariants())
			clone.getVariants().add(variant.clone());
		if (getHeatNet() != null)
			clone.setHeatNet(getHeatNet().clone());
		if (getCostSettings() != null)
			clone.setCostSettings(getCostSettings().clone());
		// TODO: clone other elements
		return clone;
	}

	public ProjectDescriptor toDescriptor() {
		ProjectDescriptor d = new ProjectDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.setVariant(isVariant());
		return d;
	}
}
