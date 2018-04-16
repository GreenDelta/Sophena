# Jahresdauerlinie des Wärmenetzes
Zu jedem Projekt gehören Angaben zum Wärmenetz (`HeatNet`), wie zum Beispiel die
Vorlauf- (`supplyTemperature`) und Rücklauftemperatur (`returnTemperature`). 

## Wärmeverluste im Netz
Für die Berechnung der projektspezifischen Jahresdauerlinie müssen die 
Netzverluste des Wärmenetzes berechnet werden. Dafür wird die Trassenlänge des
Wärmenetzes (`length`, in [m]) mit dem durchschnittlichen Wärmeverlust des
Wärmenetzes (`powerLoss`, in [W/m]) multipliziert:

```julia
netLoad = (length * powerLoss) / 1000
```

Diese Netzlast (in [kW]) wird zu jeder Jahresstunde in der projektspezifischen 
Jahresdauerlinie hinzuaddiert (siehe Funktionen in `ProjectLoad`). Die Werte
für die Trassenlänge und die durchschnittliche Verlustleistung können pauschal
angegeben oder aus den angegebenen Wärmeleitungen berechnet werden. Beim Anlegen
eines Projekts wird der Standardwert für die Verlustleistung auf 20 W/m gesetzt.

### Berechnung aus Wärmeleitungen
Wärmeleitungen (Typ `Pipe`) aus der Produktdatenbank können in Projekten
verwendet und mit projektspezifischen Informationen, wie zum Beispiel der Länge,
angereichert werden (Typ `HeatNetPipe`). Die Trassenlänge und der Wärmeverlust
des Wärmenetzes können dann aus den Wärmeleitungen des Projekts berechnet werden.
Bei der Trassenlänge ist es wichtig, dass diese sowohl den Zulauf als auch den
Rücklauf beinhaltet und es zwei Typen von Rohren gibt: `PipeType.UNO` sowie
`PipeType.DUO`. Bei den `DUO`-Rohren ist sowohl der Zulauf als auch der
Rücklauf in einem Rohr enthalten.

Dazu ist in den Produktdaten bei jedem Rohr der Wärmedurchgangskoeffizient U
angegeben. Der Wärmeverlust des Rohrs errechnet sich dann mit der Formel
(T_Netz – T_Boden) * U. Dabei ist die Netztemperatur der Mittelwert von Vor-
und Rücklauftemperatur, die Bodentemperatur wird standardmäßig mit 10°C
angenommen. Die durchschnittliche Verlustleistung des Netzes in
Watt/Trassenmeter berechnet sich dann aus der Summe der Rohr-Verlustleistungen
dividiert durch die Trassenlänge.

Beispiel: Für ein Netz werden 2000m eines Uno-Rohrs (berechnete Verlustleistung:
8 W/m) und 1000m eines Duo-Rohrs (berechnete Verlustleistung: 12 W/m) benötigt.
Die Trassenlänge beträgt dann 2000m, die gesamte Verlustleistung des Netzes
28.000 Watt. Die durchschnittliche Verlustleistung des Netzes beträgt somit
28.000 / 2000 = 14 Watt/Trassenmeter.


Die Wärmeverlustleistung eines Rohres `i` errechnet sich wie folgt:

	Verlustleistung i [W/m] = ( Temperatur Netz - Temperatur Boden ) * U-Wert i
	
Die Temperatur des Netzes ist dabei der Mittelwert aus Vorlauf- und 
Rücklauftemperatur. Die Temperatur des Bodens wird immer mit 10°C angenommen. 
Der U-Wert wird in der jeweiligen Produktspezifikation der Wärmeleitung 
angegeben. Der Wärmeverlust des Netzes insgesamt berechnet sich dann aus dem
gewichteten Mittel der Wärmeverluste und Längen der einzelnen Rohrleitungen:

	Verlustleistung [W/m] = Summe (Verlustleistung i * Länge i ) / Trassenlänge 

## Unterbrechnungen
Es ist möglich, dass ein Wärmenetz für einen Zeitraum im Jahr abgeschaltet wird
(z.B. Sommerabschaltung). Für diesen Zeitraum wird die projektspezifische 
Lastkurve einfach auf 0 gesetzt.
    