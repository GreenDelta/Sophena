Wärmenetz
=========
Zu jedem Projekt gehören Angaben zum Wärmenetz (`HeatNet`), wie zum Beispiel die
Vorlauf- (`supplyTemperature`) und Rücklauftemperatur (`returnTemperature`). 

Wärmeverluste im Netz
---------------------
Für die Berechnung der projektspezifischen Jahresdauerlinie müssen die 
Netzverluste des Wärmenetzes berechnet werden. Dafür wird die Trassenlänge des Wärmenetzes 
(`length`, in [m]) mit dem Wärmeverlust des Wärmenetzes (`powerLoss`, in [W/m]) multipliziert:

	Netzlast [kW] = Trassenlänge [m] * Verlustleistung [W/m] / 1000
	
Diese Netzlast wird zu jeder Jahresstunde in der projektspezifischen 
Jahresdauerlinie hinzuaddiert (siehe Funktionen in `ProjectLoad`).

Wärmeleitungen
--------------
Wärmeleitungen (`Pipe`) aus der Produktdatenbank können in Projekten verwendet
und mit projektspezifischen Informationen angereichert werden (`HeatNetPipe`).
Die Trassenlänge und der Wärmeverlust des Wärmenetzes können aus den 
Wärmeleitungen des Projekts berechnet werden. Die Trassenlänge ist dabei einfach
die Summe der jeweiligen Längen der Wärmeleitungen:

	Trassenlänge [m] = Summe ( Länge Wärmeleitung i [m])
	
Die Wärmeverlustleistung eines Rohres `i` errechnet sich wie folgt:

	Verlustleistung i [W/m] = ( Temperatur Netz - Temperatur Boden ) * U-Wert i
	
Die Temperatur des Netzes ist dabei der Mittelwert aus Vorlauf- und 
Rücklauftemperatur. Die Temperatur des Bodens wird immer mit 10°C angenommen. 
Der U-Wert wird in der jeweiligen Produktspezifikation der Wärmeleitung 
angegeben. Der Wärmeverlust des Netzes insgesamt berechnet sich dann aus dem
gewichteten Mittel der Wärmeverluste und Längen der einzelnen Rohrleitungen:

	Verlustleistung [W/m] = Summe (Verlustleistung i * Länge i ) / Trassenlänge 

Unterbrechnungen
----------------
Es ist möglich, dass ein Wärmenetz für einen Zeitraum im Jahr abgeschaltet wird
(z.B. Sommerabschaltung). Für diesen Zeitraum wird die projektspezifische 
Lastkurve einfach auf 0 gesetzt.
    