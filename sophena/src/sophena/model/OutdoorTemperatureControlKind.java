package sophena.model;

/// Defines when an outdoor-temperature-controlled producer is enabled.
public enum OutdoorTemperatureControlKind {

	/// The producer is enabled when the outdoor temperature is equal or above the
	/// configured temperature of the producer.
	From,

	/// The producer is enabled when the outdoor temperature is equal or below the
	/// configured temperature of the producer.
	Until
}
