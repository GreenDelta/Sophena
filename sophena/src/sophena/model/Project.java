package sophena.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import sophena.model.descriptors.ProjectDescriptor;

@Entity
@Table(name = "tbl_projects")
public class Project extends RootEntity {

	@OneToOne
	@JoinColumn(name = "f_project_folder")
	public ProjectFolder folder;

	@Column(name = "project_duration")
	public int duration;

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

	@JoinColumn(name = "f_project")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<FlueGasCleaningEntry> flueGasCleaningEntries = new ArrayList<>();

	@Override
	public Project copy() {
		var clone = new Project();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.folder = folder;
		clone.duration = duration;
		clone.weatherStation = weatherStation;

		for (var consumer : consumers) {
			clone.consumers.add(consumer.copy());
		}
		for (var producer : producers) {
			clone.producers.add(producer.copy());
		}
		if (heatNet != null) {
			clone.heatNet = heatNet.copy();
		}
		if (costSettings != null) {
			clone.costSettings = costSettings.copy();
		}
		cloneProductEntries(clone);
		for (var e : flueGasCleaningEntries) {
			clone.flueGasCleaningEntries.add(e.copy());
		}
		return clone;
	}

	private void cloneProductEntries(Project clone) {
		Map<String, Product> productMap = new HashMap<>();
		for (Product ownProduct : ownProducts) {
			var cp = ownProduct.copy();
			cp.projectId = clone.id;
			clone.ownProducts.add(cp);
			productMap.put(ownProduct.id, cp);
		}
		for (var entry : productEntries) {
			var ce = entry.copy();
			clone.productEntries.add(ce);
			if (ce.product == null || ce.product.projectId == null)
				continue;
			ce.product = productMap.get(ce.product.id);
		}
	}

	public ProjectDescriptor toDescriptor() {
		var d = new ProjectDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		return d;
	}
}
