package sophena.model;

public class ConvertType {
	
	public static FundingType ProductTypeToFundingType(ProductType productType)
	{
		switch (productType) {
		case BIOMASS_BOILER:
			return  FundingType.BiomassBoiler;
		case BOILER_ACCESSORIES:
			return FundingType.BoilerAccessories;
		case BOILER_HOUSE_TECHNOLOGY:
			return FundingType.BoilerHouseTechnology;
		case BUFFER_TANK:
			return FundingType.BufferTank;
		case BUILDING:
			return FundingType.Building;
		case COGENERATION_PLANT:
			return FundingType.CogenerationPlant;
		case ELECTRIC_HEAT_GENERATOR:
			return FundingType.ElectricHeatGenerator;
		case FLUE_GAS_CLEANING:
			return FundingType.FlueGasCleaning;
		case FOSSIL_FUEL_BOILER:
			return FundingType.FossilFuelBoiler;
		case HEATING_NET_CONSTRUCTION:
			return FundingType.HeatingNetConstruction;
		case HEATING_NET_TECHNOLOGY:
			return FundingType.HeatingNetTechnology;
		case HEAT_PUMP:
			return FundingType.HeatPump;
		case HEAT_RECOVERY:
			return FundingType.HeatRecovery;
		case OTHER_EQUIPMENT:
			return FundingType.OtherEquipment;
		case OTHER_HEAT_SOURCE:
			return FundingType.OtherHeatSource;
		case PIPE:
			return FundingType.Pipe;
		case PLANNING:
			return FundingType.Planning;
		case SOLAR_THERMAL_PLANT:
			return FundingType.SolarThermalPlant;
		case TRANSFER_STATION:
			return FundingType.TransferStation;
		default:
			throw new IllegalArgumentException("Unexpected value: " + productType);
		}
	}
}
