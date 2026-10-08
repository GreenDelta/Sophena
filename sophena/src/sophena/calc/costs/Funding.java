package sophena.calc.costs;

import sophena.model.FundingType;
import sophena.model.ProductType;
import sophena.model.Project;

public class Funding {

	public static double get(Project project, CostResult r) {
		return project != null && project.costSettings != null
			? project.costSettings.funding + getForFundingPercent(project, r)
			: 0;
	}

	private static double getForFundingPercent(Project project, CostResult r) {
		if (project.costSettings.fundingPercent == 0)
			return 0;
		double total = 0;
		var fundingTypes = project.costSettings.fundingTypes;
		var factor = project.costSettings.fundingPercent / 100;
		for (var item : r.items) {
			var type = fundingTypeOf(item.investment.productType());
			if (type == null)
				continue;
			if ((fundingTypes & type.getValue()) > 0) {
				total += factor * item.investment.initialInvestment();
			}
		}
		return total;
	}

	private static FundingType fundingTypeOf(ProductType type) {
		return switch (type) {
			case BIOMASS_BOILER -> FundingType.BiomassBoiler;
			case BOILER_ACCESSORIES -> FundingType.BoilerAccessories;
			case BOILER_HOUSE_TECHNOLOGY -> FundingType.BoilerHouseTechnology;
			case BUFFER_TANK -> FundingType.BufferTank;
			case BUILDING -> FundingType.Building;
			case COGENERATION_PLANT -> FundingType.CogenerationPlant;
			case ELECTRIC_HEAT_GENERATOR -> FundingType.ElectricHeatGenerator;
			case FLUE_GAS_CLEANING -> FundingType.FlueGasCleaning;
			case FOSSIL_FUEL_BOILER -> FundingType.FossilFuelBoiler;
			case HEATING_NET_CONSTRUCTION -> FundingType.HeatingNetConstruction;
			case HEATING_NET_TECHNOLOGY -> FundingType.HeatingNetTechnology;
			case HEAT_PUMP -> FundingType.HeatPump;
			case HEAT_RECOVERY -> FundingType.HeatRecovery;
			case OTHER_EQUIPMENT -> FundingType.OtherEquipment;
			case OTHER_HEAT_SOURCE -> FundingType.OtherHeatSource;
			case PIPE -> FundingType.Pipe;
			case PLANNING -> FundingType.Planning;
			case SOLAR_THERMAL_PLANT -> FundingType.SolarThermalPlant;
			case TRANSFER_STATION -> FundingType.TransferStation;
			case null, default -> null;
		};
	}
}
