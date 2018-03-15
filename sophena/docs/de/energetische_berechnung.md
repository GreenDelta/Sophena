# Energetischen Berechnung


## Heizwert und Brennstoffmenge
Die Simulationsrechnung in Sophena berechnet die Wärmemengen, welche die
jeweiligen Erzeuger (`Producer`) produzieren müssen (unter Einbezug von
Nutzungsgrad etc., siehe unten), um den Wärmebedarf zu decken. Jedem Erzeuger
kann ein Energieträger/Brennstoff (`Fuel`) zugewiesen werden. Der Energieträger
hat wiederum einen Heizwert (`calorificValue`), mit dem dann die benötigten
Brennstoffmengen (`FuelDemand`) zurückgerechnet werden können.

Alle Brennstoffe werden in einer Referenzeinheit (`unit`) angegeben (z.B. m3 für
Erdgas). Der angegebene Heizwert entspricht der Menge an Wärme (in kWh) die pro
Referenzeinheit des Brennstoffs erzeugt werden kann (z.B. kWh/m3 für Erdgas).
Um die Brennstoffmenge aus einer erzeugten Wärmemenge (`producedHeat` in kWh) zu
berechnen, teilt man diese Wärmemenge durch den Heizwert:

```java
double fuelDemand = producedHeat / calorificValue;
``` 
Für Holzbrennstoffe wird der Heizwert in kWh pro Tonnen absolut trockener Masse
(t atro) am Brennstoff gespeichert (also kWh/t atro). Für reale Holzbrennstoffe
muss der Heizwert unter berücksichtigung des Wassergehalts (`waterContent`)
wie folgt berechnet werden:

```java
calorificValue = woodMass * ((1 - waterContent) * calorificValue - waterContent * 680);
```

Dabei ist `680` die Verdampfungsenergie von Wasser (680 kWh/t). Wenn die Menge
des Holzbrennstoffs nicht in Tonnen sondern in Ster (`WoodAmountType.LOGS`) oder
Schüttraummeter (`WoodAmountType.CHIPS`) angegeben wird, so muss zunächst die
äuquivalente Masse in Tonnen (`woodMass`) für eine Einheit Ster bzw.
Schüttraummeter berechnet werden und in die obige Gleichung eingesetzt werden.
Ist die Menge des Holzbrennstoffs schon in Tonnen (feuchter Holzmasse,
`WoodAmountType.MASS`) angegeben, so wird `woodMass = 1` in die obige Formel
eingesetzt.

Die Umrechung von Ster und Schüttraummeter erfolgt über die Formel:

```
woodMass = f * (density/1000) / (1 - waterContent))
```

Die Dichte (`density`) wird dabei in kg/FM (FM = Festmeter = 1 m3 festes Holz)
angegeben (Faktor 1/1000 zur Umrechnung in Tonnen). Der Faktor `f` ist Ster
gleich `0.7` und für Schüttraummeter `0.4`. 


## Brennstoffenergie

Die Wärmemenge $Q_{fuel}$ [kWh], die durch die Verbrennung einer Menge $a$ 
eines Brennstoffs erzeugt werden kann, wird wie folgt berechnet:

$$ Q_{fuel} = a * cv $$

Die Brennstoffmenge $a$ wird in der Referenzeinheit des Brennstoffs angegeben. 
$cv$ ist der Heizwert des Brennstoffs in kWh pro Referenzeinheit.


## Volllaststunden
Die Volllaststunden $t_{full}$ [h] eines Kessels ergeben sich aus der erzeugten
Wärme $Q_{gen}$ [kWh] und der thermischen Nennleistung $P_{max,th}$ [kW] 
(= maximale Leistung) des Kessels:

$$ t_{full} = \frac{Q_{gen}}{P_{max,th}} $$

Die Volllaststunden entsprechen somit der Zeit in Stunden, die der Kessel bei
maximaler Leistung betrieben werden müsste, um die gegebene Wärme zu erzeugen.

Die entsprechende Hilfsfunktion ist:

```java
    Producer p = ...
    double fullLoadHours = FullLoadHours.get(p, producedHeat);
```

## Nutzungsdauer
Die Nutzungsdauer eines Kessels ist die Anzahl der Stunden im Jahr, die der
Kessel läuft. Im Berechnungsergebnis von Sophena wird für jeden Kessel ein
Array berechnet, welches die produzierte Wärme für die jeweilige Jahresstunde
enthält. Die Nutzungsdauer lässt sich daraus direkt bestimmen, indem man die
Anzahl der Stunden zählt, in denen das Array einen Wert enthält.

Die Hilfsfunktion dafür ist:

```java
UsageDuration.get(energyResult, producer);
```

## Nutzungsgrad
Der Nutzungsgrad $ur$ ist allgemein das Verhältnis aus nutzbar gemachter Energie
zu zugeführter Energie und wird aus dem Wirkungsgrad $er$ und dem
Bereitschaftswirkungsgrad $sr$ berechnet:

$$ ur = er * sr $$

Der Wirkungsgrad des Kessels ist eine Herstellerangabe. Der
Bereitschaftswirkungsgrad $sr$ wird wie folgt berechnet:

$$ sr = \frac{1}{(\frac{t_u}{t_{full}}-1)*sl + 1} $$

Dabei sind $t_u$ [h] die Nutzungsdauer (z.B. 8760 Stunden) des Kessels,
$t_{full}$ [h] die Volllaststunden und $sl$ der spezifische Bereitschaftsverlust.
Als spezifischer Bereitschaftsverlust wird in Sophena immer ein empirischer Wert
für kleine Kessel von 0.014 angenommen.

Die Klasse `UtilisationRate` enthält entsprechende Funktionen zur Berechnung des
Nutzungsgrades. Der Nutzungsgrad eines Kessels kann jedoch auch direkt von
Nutzer angegeben werden. Dies wird in der entsprechenden Hilfsfunktion
berücksichtigt:

```java
double ur = UtilisationRate.get(producer, energyResult);
```

Wie oben erwähnt ist der Wirkungsgrad eine Herstellerangabe. Jedoch ist es bei
Verbrauchsangaben in Sophena möglich den Nutzungsgrad anzugegben. Daraus lässt
sich dann der Wirkungsgrad durch Umstellen der obigen Formel berechnen:

$$ er = \frac{ur}{sr} = ur * [(\frac{t_u}{t_{full}}-1)*sl + 1] $$

Auch dafür gibt es wieder eine Hilfsfunktion:

```java
double eta = EfficiencyRate.get(utilisationRate, loadHours);
```

## Stromerzeugung
Die erzeugte Menge an Strom ${E_{gen}}$ [kWh] wird aus den Volllaststunden
$t_{full}$ [h] und der elektrischen Nennleistung $P_{max,el}$ [kW] einer
KWK-Anlage berechnet:

$$ E_{gen} = t_{full} * P_{max,el} $$

Da die Volllaststunden aus der erzeugten Wärme berechnet werden, sieht die
Hilfsfunktion so aus:

```java
Producer p = ...
double generatedElectricity = GeneratedElectricity.get(p, generatedHeat);
```

## Eigenstrombedarf
Der Eigenstrombedarf wird nicht aus den Daten des ausgewählten Kessels berechnet,
da eine vernünftige Abschätzung auf Basis der dort angegebenen elektrischen
Anschlussleistung sehr schwierig ist. Stattdessen wird dafür bei den allgemeinen
Angaben eine Kennzahl angegeben, diese %-Angabe bezieht sich auf die im Heizhaus
erzeugte Wärmemenge, als Default werden 1,5 % angegeben. Werden also z.B. 
2000 MWh Wärme pro Jahr erzeugt, so würde im Defaultfall der Eigenstrombedarf
mit 2000 * 0,015 = 30 MWh = 30.000 kWh abgeschätzt werden.

Der Anteil wird derzeit in den `CostSettings` eines Projekts gespeichert. Die
Hilfsfunktion zur Berechnung des Eigenstrombedarfs sieht entsprechend so aus:

```java
double usedElectricity = UsedElectricity.get(producedHeat, costSettings);
```

## Wärmerückgewinnung
In erster Näherung kann dieser Effekt linear berechnet werden. Dazu teilt man
die in den Produktdaten angegebene Leistung des Abgaswärmetauschers (AWT) durch
die dort ebenfalls angegebene Leistung des passenden Wärmeerzeugers. Diesen Wert
addiert man zu 1 und erhält damit den Faktor der Leistungssteigerung. Bei den
Berechnungen müssen die Wärmeerzeuger-Maximal- und Minimalleistungen dann mit
diesem Faktor multipliziert werden, im gleichen Maß muss auch der angegebene
Wirkungsgrad erhöht werden.

Beispiel für ein BHKW:

|                                         |        |
|-----------------------------------------|--------|
| Leistung AWT:                           | 100 kW |
| Thermische Leistung des passenden BHKW: | 250 kW |

Damit ergibt sich ein Faktor von 1 + (100 / 250) =  1,4.

Im Projekt wird ein BHKW mit einer (thermischen) Maximalleistung von 200 kW und
Minimalleistung von 130 kW und einem (thermischen) Wirkungsgrad von 35%
eingeplant. Für die Berechnungen müssen nun folgende Werte verwendet werden:

|                  |                   |
|------------------|-------------------|
| Maximalleistung: | 200 * 1,4 = 280   |
| Minimalleistung: | 130 * 1,4 = 182   |
| Wirkungsgrad:    | 35 % * 1,4 = 49 % |

An der elektrischen Leistung des BHKW ändert sich nichts. Die Hilfsfunktionen
zum Einbezug der Wärmerückgewinnung sind in der Klasse `Producers` implementiert.
Diese werden entsprechend in der Berechnung und Simulation verwendet:

```java
Producers.minPower(producer);
Producers.maxPower(producer);
Producers.efficiencyRate(producer);
```

## Genutzte Wärme
Die Genutze Wärme ist die erzeugte Wärme insgesamt abzüglich der
Verteilungsverluste im Netz.

Die Hilfsfunktion dafür ist:

```java
double usedHeat = UsedHeat.get(projectResult);
```

## Primärenergiefaktor der Nahwärme
Der Primärenergiefaktor des Wärmenetzes $pef_{net}$ ist eine Kennzahl, die
unter den weiteren Ergebnissen ausgewiesen und wie folgt berechnet wird:

$$ pef_{net} = \frac{ \sum_{i} {Q_{fuel,i}} * pef_{fuel,i} + (E_{use,i} - E_{gen,i}) * pef_{el} } {Q_u} $$ 

Dabei sind:

|                 |                                          |
|-----------------|------------------------------------------| 
| $Q_{fuel,i}$    | die Brennstoffenergie für Erzeuger $i$   |
| $pef_{fuel,i}$  | der Primärenergiefaktor des Brennstoffs  |
| $E_{use,i}$     | Eigenstrombedarf des Erzeugers           |
| $E_{gen,i}$     | Stromerzeugung in der Anlage             |
| $pef_{el}$      | Primärenergiefaktor von Strom            |
| $Q_u$           | Genutzte Wärme                           |

Die Hilfsfunktion dafür ist:

```java
double pef = PrimaryEnergyFactor.get(project, projectResult);
```
