## Erzeugerprofile
Erzeugerprofile können als CSV-Datei mit dem folgenden Format importiert werden:

* die erste Zeile wird als Überschrift ignoriert
* die erste Spalte enthält die jeweilige Jahresstunde: 1 bis 8760
* die zweite Spalte enthält die maximale Leistung in kW
* die dritte Spalte enthält die minimale Leistung in kW

```julia
using DelimitedFiles

open("example_producer_profile.csv", "w") do stream
  writedlm(stream, [["hour", "max", "min"]], ";")
  for i = 1:8760
    writedlm(stream, [[i, 42000, 21000]], ";")
  end
end
```

## Lastgänge
Der Lastgang eines Abnehmers kann ebenfalls als CSV-Datei importiert werden:

* die erste Zeile wird als Überschrift ignoriert
* die erste Spalte enthält die jeweilige Jahresstunde: 1 bis 8760
* die zweite Spalte enthält die dynamische (temperaturabhängige) Last in kW
* die dritte Spalte enthält die statische Last in kW (z.B. der Warmwasseranteil)

```julia
using DelimitedFiles

open("example_consumer_profile.csv", "w") do stream
  writedlm(stream, [["hour", "dynamic", "static"]], ";")
  for i = 1:8760
    writedlm(stream, [[i, 15000, 15000]], ";")
  end
end
```
