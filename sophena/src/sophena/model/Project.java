package sophena.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
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

	@JoinColumn(name = "f_project")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<Producer> producers = new ArrayList<>();

	@JoinColumn(name = "f_project")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<Consumer> consumers = new ArrayList<>();

	@OneToOne
	@JoinColumn(name = "f_weather_station")
	public WeatherStation weatherStation;

	@JoinColumn(name = "f_cost_settings")
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	public CostSettings costSettings;

	@JoinColumn(name = "f_heat_net")
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	public HeatNet heatNet;

	@JoinColumn(name = "f_project")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<ProductEntry> productEntries = new ArrayList<>();

	@JoinColumn(name = "f_project")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<Product> ownProducts = new ArrayList<>();

	@ElementCollection
	@CollectionTable(name = "tbl_flue_gas_cleaning_entries",
			joinColumns = @JoinColumn(name = "f_project"))
	public final List<FlueGasCleaningEntry> flueGasCleaningEntries = new ArrayList<>();

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
		cloneProductEntries(clone);
		for (FlueGasCleaningEntry e : flueGasCleaningEntries) {
			clone.flueGasCleaningEntries.add(e.clone());
		}
		return clone;
	}

	private void cloneProductEntries(Project clone) {
		Map<String, Product> productMap = new HashMap<>();
		for (Product ownProduct : ownProducts) {
			Product cp = ownProduct.clone();
			cp.projectId = clone.id;
			clone.ownProducts.add(cp);
			productMap.put(ownProduct.id, cp);
		}
		for (ProductEntry entry : productEntries) {
			ProductEntry ce = entry.clone();
			clone.productEntries.add(ce);
			if (ce.product == null || ce.product.projectId == null)
				continue;
			ce.product = productMap.get(ce.product.id);
		}
	}

	public ProjectDescriptor toDescriptor() {
		ProjectDescriptor d = new ProjectDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		return d;
	}
}
