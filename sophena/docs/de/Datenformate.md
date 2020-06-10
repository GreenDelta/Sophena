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
    day = round(Int, i / 24) % 7 + 1
    val = 15000 * day / 10 + 10000
    writedlm(stream, [(i, val, 0.1 * val)], ";")
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
    writedlm(stream, [(i, rand()*15000, rand()*15000)], ";")
  end
end
```

```smalltalk
|nl text rand|

nl := (Character cr asString), (Character lf asString).
text := 'Jahresstunde;Heizlast;Warmwasser', nl.
rand := Random new.
1 to: (365 * 24) do: [ :i |
  |dynamic static|
  dynamic := (rand nextInt: 500) * 1000.
  static := (rand nextInt: 500) * 1000.
  text := text,
    (i asString), ';',
    dynamic asString, ';',
    static asString,
    nl.
].

'out.txt' asFileReference writeStreamDo: [ :stream |
  stream nextPutAll: text.
].
```
