package main

import (
	"encoding/csv"
	"fmt"
	"os"
	"strconv"
	"strings"
)

// The CsvModel contains all data from CSV files in the data folder.
type CsvModel struct {
	BuildingStates   []*BuildingState
	ProductGroups    []*ProductGroup
	Fuels            []*Fuel
	Manufacturers    []*Manufacturer
	Boilers          []*Boiler
	BufferTanks      []*BufferTank
	Pipes            []*Pipe
	HeatRecoveries   []*HeatRecovery
	FlueGasCleanings []*FlueGasCleaning
	TransferStations []*TransferStation
}

// ReadCsvModel reads all CSV data into memory and links them
func ReadCsvModel() *CsvModel {
	model := &CsvModel{}
	model.readBuildingStates()
	model.readProductGroups()
	model.readFuels()
	model.readManufacturers()
	model.readBoilers()
	model.readBufferTanks()
	model.readPipes()
	model.readHeatRecoveries()
	model.readFlueGasCleanings()
	model.readTransferStations()
	return model
}

func (model *CsvModel) readBuildingStates() {
	model.BuildingStates = make([]*BuildingState, 0)
	fn := func(row []string) {
		s := BuildingState{}
		s.ID = cStr(row, 0)
		s.Name = cStr(row, 1)
		s.Type = BuildingType(cStr(row, 2))
		s.HeatingLimit = cFlo(row, 3)
		s.AntifreezingTemperature = cFlo(row, 4)
		s.WaterFraction = cFlo(row, 5)
		s.LoadHours = cInt(row, 6)
		s.IsDefault = cBool(row, 7)
		s.Index = cInt(row, 8)
		model.BuildingStates = append(model.BuildingStates, &s)
	}
	eachCsvRow("data/csv/building_states.csv", fn)
}

func (model *CsvModel) readProductGroups() {
	model.ProductGroups = make([]*ProductGroup, 0)
	eachCsvRow("data/csv/product_groups.csv", func(row []string) {
		pg := ProductGroup{}
		pg.IsProtected = true
		pg.ID = cStr(row, 0)
		pg.Index = cInt(row, 1)
		pg.Type = cStr(row, 2)
		pg.Name = cStr(row, 3)
		pg.FuelGroup = cStr(row, 4)
		pg.Duration = cInt(row, 5)
		pg.Repair = cFlo(row, 6)
		pg.Maintenance = cFlo(row, 7)
		pg.Operation = cFlo(row, 8)
		model.ProductGroups = append(model.ProductGroups, &pg)
	})
}

func (model *CsvModel) readFuels() {
	model.Fuels = make([]*Fuel, 0)
	fn := func(row []string) {
		f := Fuel{}
		f.IsProtected = true
		f.ID = cStr(row, 0)
		f.Name = cStr(row, 1)
		f.Group = cStr(row, 2)
		f.Unit = cStr(row, 3)
		f.CalorificValue = cFlo(row, 4)
		f.Density = cFlo(row, 5)
		f.Co2Emissions = cFlo(row, 6)
		f.PrimaryEnergyFactor = cFlo(row, 7)
		f.AshContent = cFlo(row, 8)
		model.Fuels = append(model.Fuels, &f)
	}
	eachCsvRow("data/csv/fuels.csv", fn)
}

func (model *CsvModel) readManufacturers() {
	model.Manufacturers = make([]*Manufacturer, 0)
	fn := func(row []string) {
		m := Manufacturer{}
		m.IsProtected = true
		m.ID = cStr(row, 0)
		m.Name = cStr(row, 1)
		m.Address = cStr(row, 2)
		m.URL = cStr(row, 3)
		m.Description = cStr(row, 4)
		m.SponsorOrder = cInt(row, 5)
		m.Logo = cStr(row, 6)
		model.Manufacturers = append(model.Manufacturers, &m)
	}
	eachCsvRow("data/csv/manufacturers.csv", fn)
}

func (model *CsvModel) readBoilers() {
	model.Boilers = make([]*Boiler, 0)
	fn := func(row []string) {
		b := Boiler{}
		productType := cStr(row, 1)
		model.mapProductData(row, &b.Product, productType)
		b.MaxPower = cFlo(row, 7)
		b.MinPower = cFlo(row, 8)
		b.EfficiencyRate = cFlo(row, 9)
		if productType != "COGENERATION_PLANT" {
			b.IsCoGenPlant = false
			b.Description = cStr(row, 10)
		} else {
			b.IsCoGenPlant = true
			b.MaxPowerElectric = cFlo(row, 10)
			b.MinPowerElectric = cFlo(row, 11)
			b.EfficiencyRateElectric = cFlo(row, 12)
			b.Description = cStr(row, 13)
		}
		model.Boilers = append(model.Boilers, &b)
	}
	eachCsvRow("data/csv/boilers_cogen_plants.csv", fn)
	eachCsvRow("data/csv/boilers.csv", fn)
	eachCsvRow("data/csv/heat_pumps.csv", fn)
}

func (model *CsvModel) readBufferTanks() {
	model.BufferTanks = make([]*BufferTank, 0)
	fn := func(row []string) {
		b := BufferTank{}
		model.mapProductData(row, &b.Product, "BUFFER_TANK")
		b.Volume = cFlo(row, 7)
		b.Diameter = cFloPtr(row, 8)
		b.Height = cFloPtr(row, 9)
		b.InsulationThickness = cFloPtr(row, 10)
		b.Description = cStr(row, 11)
		model.BufferTanks = append(model.BufferTanks, &b)
	}
	eachCsvRow("data/csv/buffer_tanks.csv", fn)
}

func (model *CsvModel) readPipes() {
	model.Pipes = make([]*Pipe, 0)
	fn := func(row []string) {
		p := Pipe{}
		model.mapProductData(row, &p.Product, "PIPE")
		p.Material = cStr(row, 7)
		p.PipeType = strings.ToUpper(cStr(row, 8))
		p.UValue = cFlo(row, 9)
		p.InnerDiameter = cFlo(row, 10)
		p.OuterDiameter = cFlo(row, 11)
		p.TotalDiameter = cFlo(row, 12)
		p.DeliveryType = cStr(row, 13)
		p.MaxTemperature = cFloPtr(row, 14)
		p.MaxPressure = cFloPtr(row, 15)
		p.Description = cStr(row, 16)
		model.Pipes = append(model.Pipes, &p)
	}
	eachCsvRow("data/csv/pipes.csv", fn)
}

func (model *CsvModel) readHeatRecoveries() {
	model.HeatRecoveries = make([]*HeatRecovery, 0)
	fn := func(row []string) {
		h := HeatRecovery{}
		model.mapProductData(row, &h.Product, "HEAT_RECOVERY")
		h.Power = cFlo(row, 7)
		h.HeatRecoveryType = cStr(row, 8)
		h.Fuel = cStr(row, 9)
		h.ProducerPower = cFlo(row, 10)
		h.Description = cStr(row, 11)
		model.HeatRecoveries = append(model.HeatRecoveries, &h)
	}
	eachCsvRow("data/csv/heat_recoveries.csv", fn)
}

func (model *CsvModel) readFlueGasCleanings() {
	model.FlueGasCleanings = make([]*FlueGasCleaning, 0)
	fn := func(row []string) {
		c := FlueGasCleaning{}
		model.mapProductData(row, &c.Product, "FLUE_GAS_CLEANING")
		c.MaxVolumeFlow = cFlo(row, 7)
		c.Fuel = cStr(row, 8)
		c.MaxProducerPower = cFlo(row, 9)
		c.MaxElectricityConsumption = cFlo(row, 10)
		c.CleaningMethod = cStr(row, 11)
		c.CleaningType = cStr(row, 12)
		c.SeparationEfficiency = cFlo(row, 13)
		c.Description = cStr(row, 14)
		model.FlueGasCleanings = append(model.FlueGasCleanings, &c)
	}
	eachCsvRow("data/csv/flue_gas_cleanings.csv", fn)
}

func (model *CsvModel) readTransferStations() {
	model.TransferStations = make([]*TransferStation, 0)
	fn := func(row []string) {
		t := TransferStation{}
		model.mapProductData(row, &t.Product, "TRANSFER_STATION")
		t.BuildingType = cStr(row, 7)
		t.OutputCapacity = cFlo(row, 8)
		t.StationType = cStr(row, 9)
		t.Material = cStr(row, 10)
		t.WaterHeating = cStr(row, 11)
		t.Control = cStr(row, 12)
		t.Description = cStr(row, 13)
		model.TransferStations = append(model.TransferStations, &t)
	}
	eachCsvRow("data/csv/transfer_stations.csv", fn)
}

func (model *CsvModel) mapProductData(row []string, e *Product, pType string) {
	e.ID = cStr(row, 0)
	e.IsProtected = true
	e.ProductGroup = model.refProductGroup(row, 2)
	e.Manufacturer = model.refManufacturer(row, 3)
	e.Name = cStr(row, 4)
	e.URL = cStr(row, 5)
	e.PurchasePrice = cFloPtr(row, 6)
	e.Type = pType
}

// eachCsvRow iterates over the given CSV file and calls the given
// function for every row in the file. The column separator should
// be a semicolon and the firts row (column header) is ignored.
func eachCsvRow(filePath string, fn func([]string)) {
	fmt.Println("Read file", filePath, "...")
	file, err := os.Open(filePath)
	check(err)
	defer file.Close()
	reader := csv.NewReader(file)
	reader.Comma = ';'
	i := 0
	for r, err := reader.Read(); r != nil; r, err = reader.Read() {
		check(err)
		i++
		if i == 1 {
			continue
		}
		fn(r)
	}
	check(err)
	fmt.Println("done")
}

func cInt(row []string, idx int) int {
	s := cStr(row, idx)
	if s == "" {
		return 0
	}
	i, err := strconv.Atoi(s)
	check(err)
	return i
}

func cFlo(row []string, idx int) float64 {
	s := cStr(row, idx)
	if s == "" {
		return 0
	}
	f, err := strconv.ParseFloat(s, 64)
	check(err)
	return f
}

func cFloPtr(row []string, idx int) *float64 {
	s := cStr(row, idx)
	if s == "" {
		return nil
	}
	f := cFlo(row, idx)
	return &f
}

func cBool(row []string, idx int) bool {
	s := cStr(row, idx)
	if s == "" {
		return false
	}
	b, err := strconv.ParseBool(s)
	check(err)
	return b
}

func cStr(row []string, idx int) string {
	return strings.TrimSpace(row[idx])
}

func (model *CsvModel) refProductGroup(row []string, idx int) *RootEntity {
	if model.ProductGroups == nil {
		fmt.Println("ERROR: no product groups in model")
		return nil
	}
	name := cStr(row, idx)
	for _, g := range model.ProductGroups {
		if g.Name == name {
			return NewReference(g.ID, g.Name)
		}
	}
	fmt.Println("ERROR: did not found product group", name)
	return nil
}

func (model *CsvModel) refFuel(row []string, idx int) *RootEntity {
	if model.Fuels == nil {
		fmt.Println("ERROR: no fuels in model")
		return nil
	}
	name := cStr(row, idx)
	for _, f := range model.Fuels {
		if f.Name == name {
			return NewReference(f.ID, f.Name)
		}
	}
	fmt.Println("ERROR: did not found fuel", name)
	return nil
}

func (model *CsvModel) refManufacturer(row []string, idx int) *RootEntity {
	if model.Manufacturers == nil {
		fmt.Println("ERROR: no manufacturers in model")
		return nil
	}
	name := cStr(row, idx)
	if name == "" {
		return nil // Manufacturer is optional
	}
	for _, m := range model.Manufacturers {
		if m.Name == name {
			return NewReference(m.ID, m.Name)
		}
	}
	fmt.Println("ERROR: did not found manufacturer", name)
	return nil
}
