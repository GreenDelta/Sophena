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

	@Override
	public Fermenter copy() {
		var clone = new Fermenter();
		clone.id = UUID.randomUUID().toString();
		clone.roofType = roofType;
		clone.targetTemperature = targetTemperature;
		clone.wallOuterRadius = wallOuterRadius;
		clone.wallStructuralThickness = wallStructuralThickness;
		clone.wallInsulationThickness = wallInsulationThickness;
		clone.wallTotalHeight = wallTotalHeight;
		clone.wallBuriedFraction = wallBuriedFraction;
		clone.roofFixedLayerThickness = roofFixedLayerThickness;
		clone.roofInsulationThickness = roofInsulationThickness;
		clone.roofMembraneHeight = roofMembraneHeight;
		clone.floorSlabThickness = floorSlabThickness;
		clone.floorInsulationThickness = floorInsulationThickness;
		clone.wallShadingFraction = wallShadingFraction;
		clone.roofShadingFraction = roofShadingFraction;
		clone.mixerPowerDensity = mixerPowerDensity;
		clone.mixerRuntime = mixerRuntime;
		clone.mixerHeatFraction = mixerHeatFraction;
		return clone;
	}
}
