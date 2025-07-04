package main

// Entity describes a general thing that is stored in the database.
type Entity interface {
	GetID() string
	GetName() string
}

// RootEntity contains the basic information of all stand-alone entities
type RootEntity struct {
	ID          string `json:"id"`
	Name        string `json:"name"`
	Description string `json:"description,omitempty"`
}

// GetID returns the ID of the entity.
func (e *RootEntity) GetID() string {
	return e.ID
}

// GetName returns the name of the entity.
func (e *RootEntity) GetName() string {
	return e.Name
}

// NewReference creates a pointer to a root entity with the given ID and
// name.
func NewReference(id, name string) *RootEntity {
	return &RootEntity{ID: id, Name: name}
}

// A BaseDataEntity is an entity that is provided with base data (background
// and company data). Base data can be protected and if this is the case
// they cannot be modified within the application.
type BaseDataEntity struct {
	RootEntity
	IsProtected bool `json:"isProtected"`
}

// A ProductGroup is a category for a specific product type. It contains
// default data for the economic calculation.
type ProductGroup struct {
	BaseDataEntity
	Type        string  `json:"type"`
	Index       int     `json:"index"`
	FuelGroup   string  `json:"fuelGroup"`
	Duration    int     `json:"duration"`
	Repair      float64 `json:"repair"`
	Maintenance float64 `json:"maintenance"`
	Operation   float64 `json:"operation"`
}

// Product defines general data of products
type Product struct {
	BaseDataEntity
	PurchasePrice *float64    `json:"purchasePrice,omitempty"`
	URL           string      `json:"url,omitempty"`
	Type          string      `json:"type"`
	ProductGroup  *RootEntity `json:"group"`
	Manufacturer  *RootEntity `json:"manufacturer"`

	// for project private products
	ProjectID string `json:"projectId,omitempty"`
}

// WeatherStation contains meta-information and temperature data of a
// DWD weather station.
type WeatherStation struct {
	BaseDataEntity
	Longitude          float64   `json:"longitude"`
	Latitude           float64   `json:"latitude"`
	Altitude           float64   `json:"altitude"`
	ReferenceLongitude float64   `json:"referenceLongitude"`
	Data               []float64 `json:"data"`
	DirectRadiation    []float64 `json:"directRadiation"`
	DiffuseRadiation   []float64 `json:"diffuseRadiation"`
}

// Fuel stores fuel data like the unit of measurement of the
// fueld and the calorific value per unit.
type Fuel struct {
	BaseDataEntity
	Group               string  `json:"group"`
	Unit                string  `json:"unit"`
	CalorificValue      float64 `json:"calorificValue"`
	Density             float64 `json:"density"`
	Co2Emissions        float64 `json:"co2Emissions"`
	PrimaryEnergyFactor float64 `json:"primaryEnergyFactor"`
	AshContent          float64 `json:"ashContent"`
}

// Manufacturer stores manufacturer information
type Manufacturer struct {
	BaseDataEntity
	Address      string `json:"address,omitempty"`
	URL          string `json:"url,omitempty"`
	SponsorOrder int    `json:"sponsorOrder"`
	Logo         string `json:"logo,omitempty"`
}

// A Boiler stores data of a boiler product that can be used in
// a heat producer.
type Boiler struct {
	Product
	MaxPower               float64 `json:"maxPower"`
	MinPower               float64 `json:"minPower"`
	EfficiencyRate         float64 `json:"efficiencyRate"`
	IsCoGenPlant           bool    `json:"isCoGenPlant"`
	MaxPowerElectric       float64 `json:"maxPowerElectric"`
	MinPowerElectric       float64 `json:"minPowerElectric"`
	EfficiencyRateElectric float64 `json:"efficiencyRateElectric"`
}

// A BufferTank stores data of a buffer tank product that can be
// used in the heating net configuration of a project.
type BufferTank struct {
	Product
	Volume              float64  `json:"volume"`
	Diameter            *float64 `json:"diameter,omitempty"`
	Height              *float64 `json:"height,omitempty"`
	InsulationThickness *float64 `json:"insulationThickness,omitempty"`
}

// A BuildingState contains default configurations of a specific
// building type and state.
type BuildingState struct {
	BaseDataEntity
	Index                   int          `json:"index"`
	IsDefault               bool         `json:"isDefault"`
	Type                    BuildingType `json:"type"`
	HeatingLimit            float64      `json:"heatingLimit"`
	AntifreezingTemperature float64      `json:"antifreezingTemperature"`
	WaterFraction           float64      `json:"waterFraction"`
	LoadHours               int          `json:"loadHours"`
}

// Pipe stores product data of pipes.
type Pipe struct {
	Product
	Material       string   `json:"material,omitempty"`
	PipeType       string   `json:"pipeType"`
	UValue         float64  `json:"uValue"`
	InnerDiameter  float64  `json:"innerDiameter"`
	OuterDiameter  float64  `json:"outerDiameter"`
	TotalDiameter  float64  `json:"totalDiameter"`
	DeliveryType   string   `json:"deliveryType,omitempty"`
	MaxTemperature *float64 `json:"maxTemperature,omitempty"`
	MaxPressure    *float64 `json:"maxPressure,omitempty"`
}

// TransferStation stores product data of transfer stations.
type TransferStation struct {
	Product
	BuildingType   string  `json:"buildingType,omitempty"`
	OutputCapacity float64 `json:"outputCapacity"`
	StationType    string  `json:"stationType,omitempty"`
	Material       string  `json:"material,omitempty"`
	WaterHeating   string  `json:"waterHeating,omitempty"`
	Control        string  `json:"control,omitempty"`
}

// HeatRecovery stores product data of heat recovery plants.
type HeatRecovery struct {
	Product
	Power            float64 `json:"power"`
	HeatRecoveryType string  `json:"heatRecoveryType,omitempty"`
	Fuel             string  `json:"fuel,omitempty"`
	ProducerPower    float64 `json:"producerPower"`
}

// FlueGasCleaning stores product data of flue gas cleaning plants.
type FlueGasCleaning struct {
	Product
	FlueGasCleaningType       string  `json:"flueGasCleaningType,omitempty"`
	MaxVolumeFlow             float64 `json:"maxVolumeFlow"`
	Fuel                      string  `json:"fuel,omitempty"`
	MaxProducerPower          float64 `json:"maxProducerPower"`
	MaxElectricityConsumption float64 `json:"maxElectricityConsumption"`
	CleaningMethod            string  `json:"cleaningMethod,omitempty"`
	CleaningType              string  `json:"cleaningType,omitempty"`
	SeparationEfficiency      float64 `json:"separationEfficiency"`
}

type SolarCollector struct {
	Product
	CollectorArea            float64 `json:"collectorArea"`
	EfficiencyRateRadiation  float64 `json:"efficiencyRateRadiation"`
	CorrectionFactor         float64 `json:"correctionFactor"`
	HeatTransferCoefficient1 float64 `json:"heatTransferCoefficient1"`
	HeatTransferCoefficient2 float64 `json:"heatTransferCoefficient2"`
	HeatCapacity             float64 `json:"heatCapacity"`
	AngleIncidenceEW10       float64 `json:"angleIncidenceEW10"`
	AngleIncidenceEW20       float64 `json:"angleIncidenceEW20"`
	AngleIncidenceEW30       float64 `json:"angleIncidenceEW30"`
	AngleIncidenceEW40       float64 `json:"angleIncidenceEW40"`
	AngleIncidenceEW50       float64 `json:"angleIncidenceEW50"`
	AngleIncidenceEW60       float64 `json:"angleIncidenceEW60"`
	AngleIncidenceEW70       float64 `json:"angleIncidenceEW70"`
	AngleIncidenceEW80       float64 `json:"angleIncidenceEW80"`
	AngleIncidenceEW90       float64 `json:"angleIncidenceEW90"`
	AngleIncidenceNS10       float64 `json:"angleIncidenceNS10"`
	AngleIncidenceNS20       float64 `json:"angleIncidenceNS20"`
	AngleIncidenceNS30       float64 `json:"angleIncidenceNS30"`
	AngleIncidenceNS40       float64 `json:"angleIncidenceNS40"`
	AngleIncidenceNS50       float64 `json:"angleIncidenceNS50"`
	AngleIncidenceNS60       float64 `json:"angleIncidenceNS60"`
	AngleIncidenceNS70       float64 `json:"angleIncidenceNS70"`
	AngleIncidenceNS80       float64 `json:"angleIncidenceNS80"`
	AngleIncidenceNS90       float64 `json:"angleIncidenceNS90"`
}

// A HeatPump stores data of a heat pump
type HeatPump struct {
	Product
	MinPower          float64   `json:"minPower"`
	RatedPower        float64   `json:"ratedPower"`
	MaxPower          []float64 `json:"maxPower"`
	Cop               []float64 `json:"cop"`
	TargetTemperature []float64 `json:"targetTemperature"`
	SourceTemperature []float64 `json:"sourceTemperature"`
}

// BiogasSubstrate stores data for biogas substrates used in biogas production
type BiogasSubstrate struct {
	BaseDataEntity
	DryMatter        float64 `json:"dryMatter"`
	OrganicDryMatter float64 `json:"organicDryMatter"`
	BiogasProduction float64 `json:"biogasProduction"`
	MethaneContent   float64 `json:"methaneContent"`
	Co2Emissions     float64 `json:"co2Emissions"`
}
