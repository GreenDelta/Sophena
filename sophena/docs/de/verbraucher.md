#Verbraucher / Wärmeabnehmer

Verbraucher bzw. Wärmeabnehmer werden in der Klasse `Consumer` definiert. 
Die Kombination aus Gebäudetyp (`BuildingType`) und Gebäudezustand 
(`BuildingState`) für einen Verbraucher bestimmt die Defaultwerte für 

* Heizgrenztemperatur (`heatingLimit`)
* Warmwasseranteil (`waterFraction`)
* Volllaststunden (`loadHours`)

Die Kombinationen dieser Werte werden in der Klasse `BuildingState` gespeichert.
Diese Werte können vom Nutzer beim jeweiligen Abnehmer überschrieben werden.

## Ermittlung des Wärmebedarfs

Bei der Berechnung des Wärmebedarfs werden von Verbrauchern wird zwischen 

* Verbrauchsgebundener Ermittlung (`consumer.demandBased = false`) sowie
* Bedarfsgebundener Ermittlung unterschieden (`consumer.demandBased = true`).

### Verbrauchsgebundene Ermittlung
Die verbrauchsgebundene Ermittlung des Wärmebedarfs richtet sich nach dem 
bisherigen Verbräuchen eines Abnehmers. Dafür wird der Nutzungsgrad der 
bisherigen Heizung benötigt:

	Nutzungsgrad = Kesselwirkungsgrad * Bereitschaftswirkungsgrad
	
Der Kesselwirkungsgrad wird direkt vom Hersteller angegeben. Der 
Bereitschaftswirkungsgrad `BW` kann so abgeschätzt werden:

	BW = 1/((Nutzungsdauer/Volllaststunden - 1) * Bereitschaftsverlust + 1)

Für die Nutzungsdauer werden 8760 Jahresstunden angesetzt und für den
Bereitschaftsverlust 0.014 für kleine Kessel. Die Volllaststunden ergeben sich
aus der Spezifikation für den Abnehmer (siehe oben). Damit vereinfacht sich die
Formel zu:

	Nutzungsgrad = Kesselwirkungsgrad/((8760/Volllaststunden - 1) * 0.014 + 1)
	
Für große Kessel ist die Formel gleich, nur wird ein Bereitschaftsverlust von
0.0055 angenommen. Durch umstellen lässt sich entsprechend der 
Kesselwirkungsgrad aus dem Nutzungsgrad und den Volllaststunden berechnen:

	Kesselwirkungsgrad = Nutzungsgrad * ((8760/Volllaststunden - 1) * 0.014 + 1)
	
Diese Formeln sind in der Klasse `BoilerEfficiency` in Hilfsfunktionen
gebündelt.
 

### Bedarfsgebundene Ermittlung
Für manche Abnehmer stehen keine Verbrauchsdaten zur Verfügung (z.B. bei 
Neubauten). 

