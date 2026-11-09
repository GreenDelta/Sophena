package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import sophena.model.AbstractEntity;

/**
 * A fermenter of a biogas plant.
 */
@Entity
@Table(name = "tbl_fermenters")
public class Fermenter extends AbstractEntity {

	/// The name of the fermenter, e.g. "Hauptfermenter" or "Nachgärer".
	@Column(name = "name")
	public String name;

	/// The ID of the fermenter into which the substrate flows from this
	/// fermenter, or `null` when there is no such fermenter. Fermenters into
	/// which no other fermenter flows receive the substrate input of the plant.
	/// It is stored as a plain ID and not as an entity reference so that the
	/// fermenters can be serialized as a flat list.
	@Column(name = "successor_id")
	public String successorId;

	@Enumerated(EnumType.STRING)
	@Column(name = "roof_type")
	public RoofType roofType;

	/// The target substrate temperature in °C.
	/// Parameter of the original fermenter simulation model: `tSetC`.
	@Column(name = "target_temperature")
	public double targetTemperature;

	/// The outer radius of the wall in m.
	/// Parameter of the original fermenter simulation model: `wRaM`.
	@Column(name = "wall_outer_radius")
	public double wallOuterRadius;

	/// The thickness of the structural wall in m.
	/// Parameter of the original fermenter simulation model: `wS`.
	@Column(name = "wall_structural_thickness")
	public double wallStructuralThickness;

	/// The thickness of the wall insulation in m.
	/// Parameter of the original fermenter simulation model: `wIm`.
	@Column(name = "wall_insulation_thickness")
	public double wallInsulationThickness;

	/// The total height of the wall in m.
	/// Parameter of the original fermenter simulation model: `wHTotalM`.
	@Column(name = "wall_total_height")
	public double wallTotalHeight;

	/// The fraction of the wall that is buried in the ground as a fraction
	/// of the total wall height, 0..1.
	/// Parameter of the original fermenter simulation model: `buriedWallFraction`.
	@Column(name = "wall_buried_fraction")
	public double wallBuriedFraction;

	/// The thickness of the fixed roof layer in m.
	/// Parameter of the original fermenter simulation model: `dIm`.
	@Column(name = "roof_fixed_layer_thickness")
	public double roofFixedLayerThickness;

	/// The thickness of the fixed-roof insulation in m.
	/// Parameter of the original fermenter simulation model: `dSdM`.
	@Column(name = "roof_insulation_thickness")
	public double roofInsulationThickness;

	/// The height of the membrane roof dome in m.
	/// Parameter of the original fermenter simulation model: `membraneRoofHeightM`.
	@Column(name = "roof_membrane_height")
	public double roofMembraneHeight;

	/// The thickness of the floor slab in m.
	/// Parameter of the original fermenter simulation model: `boSM`.
	@Column(name = "floor_slab_thickness")
	public double floorSlabThickness;

	/// The thickness of the floor insulation in m.
	/// Parameter of the original fermenter simulation model: `boIm`.
	@Column(name = "floor_insulation_thickness")
	public double floorInsulationThickness;

	/// The shaded fraction of the wall, 0..1.
	/// Parameter of the original fermenter simulation model: `wallShadingFraction`.
	@Column(name = "wall_shading_fraction")
	public double wallShadingFraction;

	/// The shaded fraction of the roof, 0..1.
	/// Parameter of the original fermenter simulation model: `roofShadingFraction`.
	@Column(name = "roof_shading_fraction")
	public double roofShadingFraction;

	/// The installed mixer power density in W/m3.
	/// Parameter of the original fermenter simulation model: `mixerInstalledPowerDensityWm3`.
	@Column(name = "mixer_power_density")
	public double mixerPowerDensity;

	/// The mixer runtime per hour in min/h.
	/// Parameter of the original fermenter simulation model: `mixerRunTimeMinPerHour`.
	@Column(name = "mixer_runtime")
	public double mixerRuntime;

	/// The mixer heat fraction in operation, 0..1.
	/// Parameter of the original fermenter simulation model: `mixerHeatFraction`.
	@Column(name = "mixer_heat_fraction")
	public double mixerHeatFraction;

	/// The inner radius of the wall in m, derived from the outer radius and
	/// the wall thicknesses.
	/// Parameter of the original fermenter simulation model: `wRim`.
	public double wallInnerRadius() {
		return wallOuterRadius - wallStructuralThickness - wallInsulationThickness;
	}

	/// Creates a new fermenter with a new ID, the given name, and default
	/// values for a typical main fermenter with a double membrane roof.
	public static Fermenter createDefault(String name) {
		var f = new Fermenter();
		f.id = UUID.randomUUID().toString();
		f.name = name;
		f.roofType = RoofType.DOUBLE_MEMBRANE;
		f.targetTemperature = 38.0;
		f.wallOuterRadius = 12.32;
		f.wallStructuralThickness = 0.10;
		f.wallInsulationThickness = 0.10;
		f.wallTotalHeight = 10.0;
		f.wallBuriedFraction = 0.50;
		f.roofFixedLayerThickness = 0.01;
		f.roofInsulationThickness = 0.10;
		f.roofMembraneHeight = 4.0;
		f.floorSlabThickness = 0.20;
		f.floorInsulationThickness = 0.10;
		f.wallShadingFraction = 0.50;
		f.roofShadingFraction = 0.50;
		f.mixerPowerDensity = 16.0;
		f.mixerRuntime = 15.0;
		f.mixerHeatFraction = 1.0;
		return f;
	}

	/// The inner volume of the fermenter in m³, calculated from the inner
	/// radius and the total wall height.
	public double volume() {
		double r = wallInnerRadius();
		return r > 0 && wallTotalHeight > 0
			? Math.PI * r * r * wallTotalHeight
			: 0;
	}

	/// Creates a copy with a new ID. Note that the `successorId` is copied as
	/// it is and needs to be mapped to the copied successor when the fermenters
	/// of a plant are copied.
	@Override
	public Fermenter copy() {
		var clone = new Fermenter();
		clone.id = UUID.randomUUID().toString();
		clone.setValuesOf(this);
		return clone;
	}

	/// Sets all values of this fermenter, except the ID, to the values of the
	/// given fermenter.
	public void setValuesOf(Fermenter other) {
		if (other == null)
			return;
		name = other.name;
		successorId = other.successorId;
		roofType = other.roofType;
		targetTemperature = other.targetTemperature;
		wallOuterRadius = other.wallOuterRadius;
		wallStructuralThickness = other.wallStructuralThickness;
		wallInsulationThickness = other.wallInsulationThickness;
		wallTotalHeight = other.wallTotalHeight;
		wallBuriedFraction = other.wallBuriedFraction;
		roofFixedLayerThickness = other.roofFixedLayerThickness;
		roofInsulationThickness = other.roofInsulationThickness;
		roofMembraneHeight = other.roofMembraneHeight;
		floorSlabThickness = other.floorSlabThickness;
		floorInsulationThickness = other.floorInsulationThickness;
		wallShadingFraction = other.wallShadingFraction;
		roofShadingFraction = other.roofShadingFraction;
		mixerPowerDensity = other.mixerPowerDensity;
		mixerRuntime = other.mixerRuntime;
		mixerHeatFraction = other.mixerHeatFraction;
	}
}
