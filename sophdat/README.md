# sophdat
sophdat generates the database packages for Sophena. It takes data from CSV
and JSON files in the `data` folder and creates zip files that can be then
imported into Sophena. There are also scripts in this repository that prepare
the CSV files for this conversion from an Excel file (i.e. `xls2csv.py`).
sophdat is a simple command line tool written in [Go](https://golang.org/).
If you have Go installed, just switch to the `sophdat` folder and compile the
tool:

```bash
cd sophdat
go build
```

This should produce a `sophdat` executable in this folder. After this, copy the
product and weather data that are *not* distributed with the repository into the
respective data folders (see below) and run the tool:

```bash
./sophdat  # or sopdat.exe on Windows
```

The `data` folder should have the following content:

* `data/csv/*.csv`: The CSV files with base and product data (see the format
    definition below)
* `data/json/cost_settings/*.json`: The default cost settings of Sophena.
* `data/json/weather_stations/*.json`: The climate data of the weather stations
    that are included in the base data of Sophena.

## Product data
In the following the format of the CSV files with the product data is described.
All CSV files should be encoded in `UTF-8` and the column separator should be a
semicolon, otherwise the conversion will not correctly work. Also, the first row
is expected to contain the column headers and is ignored. The entries can
have leading and trailing space characters in order to align the entries in a
column so that they are easier to read in plain text editors (see the
`csvfmt.py` script). These leading and trailing space characters are removed
when generating the data packages.

### `boilers.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. max_power
9. min_power
10. eff. rate
11. description
12. key
```

### `boilers_cogen_plants.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. max_power
9. min_power
10. eff. rate
11. max_power_el
12. min_power_el
13. eff. rate el
14. description
15. key
```

### `buffer_tanks.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. volume
9. diameter
10. height
11. insulation
12. description
13. key
```

### `building_states.csv`

```
0. id
1. name
2. type
3. heatingLimit
4. antifreezingTemperature
5. waterFraction
6. loadHours
7. isDefault
8. index
```

### `flue_gas_cleanings.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. max. volume flow
9. fuel
10. max. producer power
11. el. demand
12. kind
13. type
14. separation rate
15. description
16. key
```

### `fuels.csv`
```
0. id
1. name
2. group
3. unit
4. calorific value
5. density
6. CO2 emissions
7. primary energy factor
8. ash content
```

### `heat_pumps.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. max_power
9. min_power
10. eff. rate
11. description
12. key
```

### `heat_recoveries.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. power
9. producer type
10. fuel
11. producer power
12. description
13. key
```

### `manufacturers.csv`

```
0. id
1. name
2. address
3. url
4. description
5. sponsor order
6. logo (as image URL)
```

### `pipes.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. material
9. type
10. u_value
11. inner diameter
12. outer diameter
13. total diameter
14. delivery type
15. max. temperature
16. max. pressure
17. description
18. key
```

### `product_groups.csv`

```
0. id
1. index
2. type
3. name
4. fuel group
5. duration
6. repair
7. maintenance
8. operation
```

### `transfer_stations.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. product line
5. name
6. url
7. price
8. building_type
9. power
10. type
11. material
12. hot water
13. control
14. description
15. key
```
