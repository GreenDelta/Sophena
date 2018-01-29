package main

import (
	"fmt"
	"os"
)

func main() {
	args := os.Args
	if len(args) < 2 {
		fmt.Println("No command given. Use `sophdat help`" +
			" to see available commands")
		return
	}

	cmd := args[1]
	switch cmd {
	case "help", "-h":
		help()
	case "pack", "-p":
		Pack()
	default:
		fmt.Println("Unknown command:", cmd, ". Use `sophdat help`"+
			" to see available commands")
	}
}

func help() {
	help := `
    sophdat - the Sophena data packaging tool

    Usage: sophdat <command>

    help, -h:   prints this help
    pack, -p:   creates the data package
    `
	fmt.Println(help)
}

func check(err error) {
	if err != nil {
		panic(err)
	}
}
