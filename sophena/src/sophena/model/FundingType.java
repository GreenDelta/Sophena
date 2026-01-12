package sophena.model;

public enum FundingType {

	BiomassBoiler(1),
	FossilFuelBoiler(2),
	CogenerationPlant(4),
	SolarThermalPlant(8),
	ElectricHeatGenerator(16),
	OtherHeatSource(32),
	BoilerAccessories(64),
	OtherEquipment(128),
	HeatRecovery(256),
	FlueGasCleaning(512),
	BufferTank(1024),
	BoilerHouseTechnology(2048),
	Building(4096),
	Pipe(8192),
	HeatingNetTechnology(16384),
	HeatingNetConstruction(32768),
	TransferStation(65536),
	Planning(131072),
	HeatPump(262144);

	private final int value;

	FundingType(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
