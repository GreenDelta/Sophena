package main

import (
	"fmt"
	"io/ioutil"
	"os"
	"path/filepath"
)

func main() {
	fmt.Println("Package data from `data` to `gen` folder ...")
	prepareOutputDir()
	Pack()
	fmt.Println("All done")
}

func prepareOutputDir() {
	_, err := os.Stat("gen")
	if err == os.ErrNotExist {
		check(os.Mkdir("gen", os.ModePerm))
	} else {
		fmt.Println("Delete files in `gen` folder")
		files, err := ioutil.ReadDir("gen")
		check(err)
		for _, file := range files {
			fmt.Println("  .. delete file", file.Name())
			check(os.Remove(filepath.Join("gen", file.Name())))
		}
	}
}

func check(err error) {
	if err != nil {
		panic(err)
	}
}
