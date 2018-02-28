# Stundenlinien
Stundenlinien (hours traces) sind eine elementare Datenstruktur in Sophena und
bilden Werte auf einen Bereich von `8760` Jahresstunden ab (`365*24`). Dabei
werden für den Februar immer 28 Tage angenommen (beim Aufbereiten von
Wetterdaten etc. wird der 29. Februar entsprechend ignoriert):

```julia
DAYS_IN_MONTH = [ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 ]
total_hours = 0
for days in DAYS_IN_MONTH
    total_hours += days * 24
end
print(total_hours)  # 8760
```

## Zeitintervalle
Betriebsunterbrechungen, Wartungsintervalle etc. werden in den Berechnungen
auf Stundenlinien angewendet und müssen entsprechend auf einen Start- und
Endindex einer Stundenlinie abgebildet werden. Im 0-basierte Index einer
Stundenlinie `[0, 8759]` entspricht der Wert am Index `0` der Stunde
`00.00 - 01.00` Uhr am `01.01.` und am Index `8759` der Stunde `23.00 - 00.00`
Uhr am `31.12.`.

## Tagesbasierte Intervalle
Bei tagesgenauen Intervallen (z.B. Betriebsunterbrechungen von Abnehmern) wird
für den Startindex die Stunde `00.00 - 01.00` Uhr und für den Endindex die
Stunde `23.00 - 00.00` Uhr des jweiligen Tags und Monats gewählt. Der Index der
ersten Stunde eines Tages in einem bestimmten Monat kann mit der folgenden
Funktion berechnet werden:

```julia
function first_hour(month, day)
    hour = 1
    for m = 1 : (month - 1)
        hour += DAYS_IN_MONTH[m] * 24
    end
    hour += ((day - 1) * 24)
    return hour
end
```

Der Index der letzten Stunde ergibt sich aus dem Index der ersten Stunde + 23
(egal, ob für 0-basierte oder 1-basierte Indizes):

```julia
function last_hour(month, day)
    return first_hour(month, day) + 23
end
```

Zum Beispiel wird der Zeitraum `07.08. - 03.09.` auf den 0-basierten 
Indixbereich `(5232, 5903)` abgebildet:


```julia
time_interval = (first_hour(8, 7) - 1, last_hour(9, 3) - 1)
println(time_interval)  # (5232, 5903)
```

Bei Anwenden eines Indexbereichs auf eine Stundenlinie gibt es zwei Fälle. Wenn
der Startindex kleiner als der Endindex ist, so wird die jeweilige Funktion auf
die Werte dazwischen angewendet (immer einschließlich der Start- und Endindizes):

```
_____||||||||||____
   start     end
```

Ist der Endindex kleiner als der Startindex (z.B. bei einem Zeitinterval vom
`24.12` bis zum `14.01` ->``), so wird die Funktion auf die Werte
außerhalb dieses Indexbereichs angewendet (wieder einschließlich der Start- und
Endindizes):

```
|||||_________||||||
   end      start
```