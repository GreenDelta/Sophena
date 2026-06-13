package sophena.calc.energy;

enum SolarPhase {
	
	/// The initial heating or warm-up phase of the system.
  /// It triggers when solar radiation begins to heat the collectors, 
  /// waiting for the collector temperature to exceed the storage tank 
  /// temperature by a defined threshold before activating circulation.
	WARM_UP,
	
	/// The normal operating phase of the solar thermal system.
  /// During this state, the solar pump is actively running, circulating 
  /// the heat transfer fluid to harvest thermal energy from the 
  /// collectors and load it into the heat storage tank.
	OPERATION,
	
	/// The safety shutdown and stagnation phase to prevent overheating.
  /// Triggered when the storage tank is full or maximum temperatures are exceeded;
  /// the pump is deactivated, allowing the fluid in the collector to vaporize 
  /// safely until the system cools down.
	STAGNATION,
}
