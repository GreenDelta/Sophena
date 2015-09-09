#Verbraucher / Wärmeabnehmer

Verbraucher bzw. Wärmeabnehmer werden in der Klasse `Consumer` definiert. 
Die Kombination aus Gebäudetyp (`BuildingType`) und Gebäudezustand 
(`BuildingState`) für einen Verbraucher bestimmt die Defaultwerte für 

* Heizgrenztemperatur (`heatingLimit`)
* Warmwasseranteil (`waterFraction`)
* Volllaststunden (`loadHours`)

Diese Werte können vom Nutzer überschrieben werden.


## Ermittlung des Wärmebedarfs

Bei der Berechnung des Wärmebedarfs werden von Verbrauchern wird zwischen 

* Verbrauchsgebundener Ermittlung (`consumer.demandBased = false`) sowie
* Bedarfsgebundener Ermittlung unterschieden (`consumer.demandBased = true`).

### Verbrauchsgebundene Ermittlung
Die verbrauchsgebundene Ermittlung des Wärmebedarfs richtet sich nach dem 
bisherigen Verbräuchen eines Abnehmers.

... Formeln

### Bedarfsgebundene Ermittlung
Für manche Abnehmer stehen keine Verbrauchsdaten zur Verfügung (z.B. bei 
Neubauten). 

