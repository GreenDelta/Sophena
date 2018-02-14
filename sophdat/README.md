# sophdat
sophdat generates the database packages for Sophena. It takes data from CSV
and JSON files in the `data` folder and creates zip files that can be then
imported into Sophena. There are also scripts in this repository that prepare
the CSV files for this conversion from an Excel file (i.e. `xls2csv.py`).

The `data` folder should have the following content:

* `data/csv/*.csv`: The CSV files with the product data (see the format
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
colum so that they are easier to read in plain text editors (see the
`csvfmt.py` script). These leading and trailing space characters are removed
when generating the data packages.

### `boilers.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. name
5. url
6. price
7. fuel
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
4. name
5. url
6. price
7. fuel
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
4. name
5. url
6. price
7. volume
8. diameter
9. height
10. insulation
11. description
12. key
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
4. name
5. url
6. price
7. max. volume flow
8. fuel
9. max. producer power
10. el. demand
11. kind
12. type
13. separation rate
14. description
15. key
```

### `fuels_non_wood.csv`

```
0. id
1. name
2. unit
3. calorificValue
4. wood
5. density
6. CO2 Emissionen
7. Primärenergiefaktor
```

### `fuels_wood.csv`

```
0. id
1. name
2. unit
3. calorificValue
4. wood
5. density
6. co2Emissions
7. Primärenergiefaktor
```

### `heat_recoveries.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. name
5. url
6. price
7. power
8. producer type
9. fuel
10. producer power
11. description
12. key
```

### `manufacturers.csv`

```
0. id
1. name
2. address
3. url
4. description
```

### `pipes.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. name
5. url
6. price
7. material
8. type
9. u_value
10. inner diameter
11. outer diameter
12. total diameter
13. delivery type
14. max. temperature
15. max. pressure
16. description
17. key
```

### `product_groups.csv`

```
0. id
1. index
2. type
3. name
4. duration
5. repair
6. maintenance
7. operation
```

### `transfer_stations.csv`

```
0. id
1. product type
2. product group
3. manufacturer
4. name
5. url
6. price
7. building_type
8. power
9. type
10. material
11. hot water
12. control
13. description
14. key
```