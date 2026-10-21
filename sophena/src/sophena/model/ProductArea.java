package sophena.model;

/// A product area groups different product types. The order in this enum is
/// equal to the display order in the user interface.
///
/// @de Produktgebiet
public enum ProductArea {

	TECHNOLOGY,

	BUILDINGS,

	HEATING_NET,

	PLANNING,

	/// This product area is only used and visible in the biogas plant related
	/// editors and result views. It should not be used in normal projects.
	BIOGAS;

}
