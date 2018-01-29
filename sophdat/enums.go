package main

// ProductType enumeration
type ProductType string

// Enum constants of ProductType
const (
	ProductBiomassBoiler          ProductType = "BIOMASS_BOILER"
	ProductFossilFuelBoiler       ProductType = "FOSSIL_FUEL_BOILER"
	ProductCogenerationPlant      ProductType = "COGENERATION_PLANT"
	ProductBoilerAccessories      ProductType = "BOILER_ACCESSORIES"
	ProductBufferTank             ProductType = "BUFFER_TANK"
	ProductHeatRecovery           ProductType = "HEAT_RECOVERY"
	ProductFlueGasCleaning        ProductType = "FLUE_GAS_CLEANING"
	ProductBoilerHouseTechnology  ProductType = "BOILER_HOUSE_TECHNOLOGY"
	ProductBuilding               ProductType = "BUILDING"
	ProductPipe                   ProductType = "PIPE"
	ProductHeatingNetConstruction ProductType = "HEATING_NET_CONSTRUCTION"
	ProductPlanning               ProductType = "PLANNING"
	ProductTransferStation        ProductType = "TRANSFER_STATION"
)

// WoodAmountType enumeration
type WoodAmountType string

// Enum constants of WoodAmountType
const (
	WoodAmountMass  WoodAmountType = "MASS"
	WoodAmountChips WoodAmountType = "CHIPS"
	WoodAmountLogs  WoodAmountType = "LOGS"
)

// BuildingType enumeration
type BuildingType string

// Enum constants of BuildingType
const (
	BuildingSingleFamilyHouse  BuildingType = "SINGLE_FAMILY_HOUSE"
	BuildingMultiFamilyHouse   BuildingType = "MULTI_FAMILY_HOUSE"
	BuildingBlockOfFlats       BuildingType = "BLOCK_OF_FLATS"
	BuildingTerraceHouse       BuildingType = "TERRACE_HOUSE"
	BuildingTowerBlock         BuildingType = "TOWER_BLOCK"
	BuildingSchool             BuildingType = "SCHOOL"
	BuildingKindergarden       BuildingType = "KINDERGARDEN"
	BuildingOfficeBuilding     BuildingType = "OFFICE_BUILDING"
	BuildingHospital           BuildingType = "HOSPITAL"
	BuildingNursingHome        BuildingType = "NURSING_HOME"
	BuildingRestaurant         BuildingType = "RESTAURANT"
	BuildingHotel              BuildingType = "HOTEL"
	BuildingCommercialBuilding BuildingType = "COMMERCIAL_BUILDING"
	BuildingOther              BuildingType = "OTHER"
)

// PipeType enumeration
type PipeType string

// Enum constants of PipeType
const (
	PipeUno PipeType = "UNO"
	PipeDuo PipeType = "DUO"
)
