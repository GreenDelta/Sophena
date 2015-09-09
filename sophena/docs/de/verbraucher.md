#Verbraucher / Wärmeabnehmer

Verbraucher bzw. Wärmeabnehmer werden in der Klasse `Consumer` definiert. Bei 
der Berechnung des Wärmebedarfs werden von Verbrauchern wird zwischen 

* Verbrauchsgebundener Ermittlung (`consumer.demandBased = false`) sowie
* Bedarfsgebundener Ermittlung unterschieden (`consumer.demandBased = true`).

Die Kombination aus Gebäudetyp (`BuildingType`) und Gebäudezustand 
(`BuildingState`) für einen Verbraucher bestimmt die Defaultwerte für 

* Heizgrenztemperatur (`heatingLimit`)
* Warmwasseranteil (`waterFraction`)
* Volllaststunden (`loadHours`)

Diese Werte können vom Nutzer überschrieben werden.



