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

	/// The outer radius of the wall in m. Model parameter: `wRaM`.
	@Column(name = "wall_outer_radius")
	public double wallOuterRadius;

	/// The thickness of the structural wall in m. Model parameter: `wS`.
	@Column(name = "wall_structural_thickness")
	public double wallStructuralThickness;

	/// The thickness of the wall insulation in m. Model parameter: `wIm`.
	@Column(name = "wall_insulation_thickness")
	public double wallInsulationThickness;

	/// The total height of the wall in m. Model parameter: `wHTotalM`.
	@Column(name = "wall_total_height")
	public double wallTotalHeight;

	/// The fraction of the wall that is buried in the ground as a fraction
	/// of the total wall height, 0..1. Model parameter: `buriedWallFraction`.
	@Column(name = "wall_buried_fraction")
	public double wallBuriedFraction;

	/// The thickness of the fixed roof layer in m. Model parameter: `dIm`.
	@Column(name = "roof_fixed_layer_thickness")
	public double roofFixedLayerThickness;

	/// The thickness of the fixed-roof insulation in m. Model parameter: `dSdM`.
	@Column(name = "roof_insulation_thickness")
	public double roofInsulationThickness;

	/// The height of the membrane roof dome in m. Model parameter: `membraneRoofHeightM`.
	@Column(name = "roof_membrane_height")
	public double roofMembraneHeight;

	/// The thickness of the floor slab in m. Model parameter: `boSM`.
	@Column(name = "floor_slab_thickness")
	public double floorSlabThickness;

	/// The thickness of the floor insulation in m. Model parameter: `boIm`.
	@Column(name = "floor_insulation_thickness")
	public double floorInsulationThickness;

	/// The shaded fraction of the wall, 0..1. Model parameter: `wallShadingFraction`.
	@Column(name = "wall_shading_fraction")
	public double wallShadingFraction;

	/// The shaded fraction of the roof, 0..1. Model parameter: `roofShadingFraction`.
	@Column(name = "roof_shading_fraction")
	public double roofShadingFraction;

	/// The installed mixer power density in W/m3. Model parameter:
	/// `mixerInstalledPowerDensityWm3`.
	@Column(name = "mixer_power_density")
	public double mixerPowerDensity;

	/// The inner radius of the wall in m, derived from the outer radius and
	/// the wall thicknesses. Model parameter: `wRim`.
	public double wallInnerRadius() {
		return wallOuterRadius - wallStructuralThickness - wallInsulationThickness;
	}

	@Override
	public Fermenter copy() {
		var clone = new Fermenter();
		clone.id = UUID.randomUUID().toString();
		clone.roofType = roofType;
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
		return clone;
	}
}
