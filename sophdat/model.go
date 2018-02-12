package main

// AnnualHours is the number of hours in a year that is used for the
// calulcation (365 * 24).
const AnnualHours = 8760

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

func (e *RootEntity) GetID() string {
	return e.ID
}

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
	Type        ProductType `json:"type"`
	Index       int         `json:"index"`
	Duration    int         `json:"duration"`
	Repair      float64     `json:"repair"`
	Maintenance float64     `json:"maintenance"`
	Operation   float64     `json:"operation"`
}

// Product defines general data of products
type Product struct {
	BaseDataEntity
	PurchasePrice *float64    `json:"purchasePrice,omitempty"`
	URL           string      `json:"url,omitempty"`
	Type          ProductType `json:"type"`
	ProductGroup  *RootEntity `json:"group"`
	Manufacturer  *RootEntity `json:"manufacturer"`

	// for project private products
	ProjectID string `json:"projectId,omitempty"`
}

// WeatherStation contains meta-information and temperature data of a
// DWD weather station.
type WeatherStation struct {
	RootEntity
	Longitude float64   `json:"longitude"`
	Latitude  float64   `json:"latitude"`
	Altitude  float64   `json:"altitude"`
	Data      []float64 `json:"data"`
}

// Fuel stores fuel data like the unit of measurement of the
// fueld and the calorific value per unit.
type Fuel struct {
	BaseDataEntity
	Unit                string  `json:"unit,omitempty"`
	CalorificValue      float64 `json:"calorificValue"`
	Density             float64 `json:"density"`
	IsWood              bool    `json:"wood"`
	Co2Emissions        float64 `json:"co2Emissions"`
	PrimaryEnergyFactor float64 `json:"primaryEnergyFactor"`
}

// Manufacturer stores manufacturer information
type Manufacturer struct {
	BaseDataEntity
	Address string `json:"address,omitempty"`
	URL     string `json:"url,omitempty"`
}

// A Boiler stores data of a boiler product that can be used in
// a heat producer.
type Boiler struct {
	Product
	MaxPower               float64        `json:"maxPower"`
	MinPower               float64        `json:"minPower"`
	Fuel                   *RootEntity    `json:"fuel,omitempty"`
	EfficiencyRate         float64        `json:"efficiencyRate"`
	WoodAmountType         WoodAmountType `json:"woodAmountType,omitempty"`
	IsCoGenPlant           bool           `json:"isCoGenPlant"`
	MaxPowerElectric       float64        `json:"maxPowerElectric"`
	MinPowerElectric       float64        `json:"minPowerElectric"`
	EfficiencyRateElectric float64        `json:"efficiencyRateElectric"`
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
	PipeType       PipeType `json:"pipeType"`
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
