package sophena.model.biogas;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import sophena.model.AnnualCostEntry;
import sophena.model.Fuel;
import sophena.model.Product;
import sophena.model.ProductEntry;
import sophena.model.ProductGroup;
import sophena.model.RootEntity;

@Entity
@Table(name = "tbl_biogas_plants")
public class BiogasPlant extends RootEntity {

	/// The duration of the biogas plant in years. This can be different
	/// from the duration of the project in which the biogas plant is used.
	@Column(name = "plant_duration")
	public int duration;

	@OneToOne
	@JoinColumn(name = "f_produced_electricity")
	public Fuel producedElectricity;

	@OneToOne
	@JoinColumn(name = "f_product_group")
	public ProductGroup productGroup;

	@OneToOne
	@JoinColumn(name = "f_electricity_price_curve")
	public ElectricityPriceCurve electricityPrices;

	/// The settings of the plant, e.g. the average hourly wage or the
	/// electricity price. A plant is always created with a settings instance.
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_settings")
	public BiogasPlantSettings settings;

	/// Size of the gas storage in m³. A `null` value means that no size is
	/// defined by the user and that a default size is calculated dynamically,
	/// see `BiogasPlants#gasStorageSizeOf(BiogasPlant)`.
	///
	/// @de Gasspeichergröße
	@Column(name = "gas_storage_size")
	public Double gasStorageSize;

	/// The share in % up to which the gas storage should be filled.
	///
	/// @de Gasspeicherbefüllungsanteil
	@Column(name = "gas_storage_filling_level")
	public double gasStorageFillingLevel;

	/// The temperature of the gas storage in °C.
	///
	/// @de Gasspeichertemperatur
	@Column(name = "gas_storage_temperature")
	public double gasStorageTemperature;

	/// The overpressure of the gas storage in mbar.
	///
	/// @de Gasspeicherüberdruck
	@Column(name = "gas_storage_overpressure")
	public double gasStorageOverpressure;

	/// The minimum runtime of the boilers in hours. This is the duration that a
	/// boiler must run at least at every start.
	///
	/// @de BHKW-Mindestlaufzeit
	@Column(name = "minimum_runtime")
	public int minimumRuntime;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "f_fermenter")
	public Fermenter fermenter;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_biogas_plant")
	public final List<BiogasPlantBoiler> boilers = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_biogas_plant")
	public final List<SubstrateProfile> substrateProfiles = new ArrayList<>();

	/// Other annual costs in EUR/a, such as administration costs, laboratory costs, etc.
	@ElementCollection
	@CollectionTable(
		name = "tbl_biogas_annual_costs",
		joinColumns = @JoinColumn(name = "f_biogas_plant")
	)
	public List<AnnualCostEntry> otherAnnualCosts = new ArrayList<>();

	/// New investments of the plant. They are calculated like the product
	/// entries of a project, see `sophena.model.ProductEntry`.
	@JoinColumn(name = "f_biogas_plant")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<ProductEntry> newInvestmentEntries = new ArrayList<>();

	/// Refurbishments and general overhauls of existing investments. Only the
	/// share that is defined in `BiogasRefurbishmentEntry#refurbishmentShare`
	/// is spent for the overhaul.
	@JoinColumn(name = "f_biogas_plant")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<BiogasRefurbishmentEntry> refurbishmentEntries = new ArrayList<>();

	/// Plant-private products, similar to `sophena.model.Project#ownProducts`.
	/// The owner of such a product is stored in `Product#projectId`.
	@JoinColumn(name = "f_project")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<Product> ownProducts = new ArrayList<>();

	@Override
	public BiogasPlant copy() {
		var copy = new BiogasPlant();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.description = description;
		copy.duration = duration;
		copy.producedElectricity = producedElectricity;
		copy.productGroup = productGroup;
		copy.electricityPrices = electricityPrices;
		copy.gasStorageSize = gasStorageSize;
		copy.gasStorageFillingLevel = gasStorageFillingLevel;
		copy.gasStorageTemperature = gasStorageTemperature;
		copy.gasStorageOverpressure = gasStorageOverpressure;
		copy.minimumRuntime = minimumRuntime;
		copy.fermenter = fermenter != null ? fermenter.copy() : null;
		for (var boiler : boilers) {
			if (boiler != null) {
				copy.boilers.add(boiler.copy());
			}
		}
		for (var p : substrateProfiles) {
			copy.substrateProfiles.add(p.copy());
		}
		copy.settings = settings != null
			? settings.copy()
			: BiogasPlantSettings.createDefault();
		for (var entry : otherAnnualCosts) {
			if (entry != null) {
				copy.otherAnnualCosts.add(entry.copy());
			}
		}
		cloneInvestments(copy);
		return copy;
	}

	/// Clones the investment related lists of this plant into the given copy.
	/// Plant-private products are cloned first and the product references of
	/// the investment entries are remapped to those clones, similar to
	/// `sophena.model.Project#cloneProductEntries(Project)`.
	private void cloneInvestments(BiogasPlant copy) {
		Map<String, Product> productMap = new HashMap<>();
		for (var ownProduct : ownProducts) {
			if (ownProduct == null)
				continue;
			var clonedProduct = ownProduct.copy();
			clonedProduct.projectId = copy.id;
			copy.ownProducts.add(clonedProduct);
			productMap.put(ownProduct.id, clonedProduct);
		}
		for (var entry : newInvestmentEntries) {
			if (entry == null)
				continue;
			var clone = entry.copy();
			copy.newInvestmentEntries.add(clone);
			clone.product = remapOwnProduct(entry.product, productMap);
		}
		for (var entry : refurbishmentEntries) {
			if (entry == null)
				continue;
			var clone = entry.copy();
			copy.refurbishmentEntries.add(clone);
			clone.product = remapOwnProduct(entry.product, productMap);
		}
	}

	private Product remapOwnProduct(
		Product product, Map<String, Product> productMap
	) {
		if (product == null || product.projectId == null)
			return product;
		var cloned = productMap.get(product.id);
		return cloned != null ? cloned : product;
	}

}
