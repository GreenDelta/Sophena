package sophena.model.biogas;

/// Defines the electricity price limit below which no market premium is paid.
///
/// @de Keine Marktprämie bei Strompreisen
public enum MarketPriceLimit {

	/// No restriction: a market premium is always paid.
	///
	/// @de Keine Einschränkung
	NONE("Keine Einschränkung"),

	/// No market premium when the electricity price is below 0 ct/kWh.
	///
	/// @de Keine Marktprämie bei Strompreisen unter 0 ct/kWh
	BELOW_ZERO("< 0 ct/kWh"),

	/// No market premium when the electricity price is 2 ct/kWh or below.
	///
	/// @de Keine Marktprämie bei Strompreisen bis einschließlich 2 ct/kWh
	BELOW_TWO("<= 2 ct/kWh");

	private final String label;

	MarketPriceLimit(String label) {
		this.label = label;
	}

	/// The label of this limit that is shown in the user interface.
	public String label() {
		return label;
	}
}
