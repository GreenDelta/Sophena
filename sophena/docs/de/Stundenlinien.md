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

Bei Tagesgenauen Intervallen wird für den Startindex die Stunde `00.00 - 01.00`
Uhr und für den Endindex dies Stunde `23.00 - 00.00` Uhr des jweiligen Tags und
Monats gewählt. Zum Beispiel wird der Zeitraum `07.08. - 03.09.` auf den
0-basierten Indixbereich `(5232, 5903)` abgebildet:


```julia
start_day = 7
start_month = 8
end_day = 3
end_month = 9

start_idx = 0
end_idx = 0

for month = 1:12
    if month < start_month
        start_idx += DAYS_IN_MONTH[month] * 24
    end
    if month < end_month
        end_idx += DAYS_IN_MONTH[month] * 24
    end
end

for day = 1:31
    if day < start_day
        start_idx += 24
    end
    if day < end_day
        end_idx += 24
    end
end

start_idx += 1
end_idx += 24

println((start_idx - 1, end_idx - 1))  # print 0-based indices
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