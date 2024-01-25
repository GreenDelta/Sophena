package main

import (
	"archive/zip"
	"encoding/json"
	"io/ioutil"
	"os"
)

// PackWriter writes data sets to a zip file.
type PackWriter struct {
	file   *os.File
	writer *zip.Writer
}

// NewPackWriter creates a new PackWriter
func NewPackWriter(filePath string) *PackWriter {
	file, err := os.Create(filePath)
	check(err)
	writer := &PackWriter{
		file:   file,
		writer: zip.NewWriter(file)}
	return writer
}

// Close closes the PackWriter
func (w *PackWriter) Close() {
	check(w.writer.Close())
	check(w.file.Close())
}

// Put writes the given entity to the given folder
func (w *PackWriter) Put(entity Entity, folder string) {
	path := folder + "/" + entity.GetID() + ".json"
	bytes, err := json.MarshalIndent(entity, "", "  ")
	check(err)
	w.PutBytes(path, bytes)
}

// PutBytes writes the given bytes to the given path
func (w *PackWriter) PutBytes(path string, bytes []byte) {
	writer, err := w.writer.Create(path)
	check(err)
	_, err = writer.Write(bytes)
	check(err)
}

// Pack creates the base data package.
func pack() {
	os.Mkdir("gen", os.ModePerm)
	csvModel := ReadCsvModel()

	// write base data
	baseData := NewPackWriter("gen/base_data.sophena")
	defer baseData.Close()
	packMeta(baseData)
	packJSONFolder("cost_settings", baseData)
	packJSONFolder("weather_stations", baseData)
	for _, s := range csvModel.BuildingStates {
		baseData.Put(s, "building_states")
	}
	for _, p := range csvModel.ProductGroups {
		baseData.Put(p, "product_groups")
	}
	for _, f := range csvModel.Fuels {
		baseData.Put(f, "fuels")
	}

	// write poduct data
	productData := NewPackWriter("gen/product_data.sophena")
	defer productData.Close()
	packMeta(productData)
	for _, m := range csvModel.Manufacturers {
		productData.Put(m, "manufacturers")
	}
	for _, b := range csvModel.Boilers {
		productData.Put(b, "boilers")
	}
	for _, b := range csvModel.BufferTanks {
		productData.Put(b, "buffers")
	}
	for _, p := range csvModel.Pipes {
		productData.Put(p, "pipes")
	}
	for _, c := range csvModel.FlueGasCleanings {
		productData.Put(c, "flue_gas_cleaning")
	}
	for _, h := range csvModel.HeatRecoveries {
		productData.Put(h, "heat_recovery")
	}
	for _, t := range csvModel.TransferStations {
		productData.Put(t, "transfer_stations")
	}
	for _, t := range csvModel.SolarCollectors {
		productData.Put(t, "solar_collectors")
	}
}

func packJSONFolder(folder string, w *PackWriter) {
	files, err := ioutil.ReadDir("data/json/" + folder)
	check(err)
	for _, file := range files {
		name := file.Name()
		p := "data/json/" + folder + "/" + name
		bytes, err := ioutil.ReadFile(p)
		check(err)
		w.PutBytes(folder+"/"+name, bytes)
	}
}

func packMeta(w *PackWriter) {
	path := "data/json/meta.json"
	bytes, err := ioutil.ReadFile(path)
	check(err)
	w.PutBytes("meta.json", bytes)
}
