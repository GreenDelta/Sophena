# Jahresdauerlinie einzelner Abnehmer

In die Berechnung der Jahresdauerlinie oder Lastkurve für einzelne Abnehmer geht
zunächst die benötigte Wärme `Q_total` [kWh] des Abnehmers für das gesamte
Jahr ein. Bei Abnehmern mit Brennstoffangaben (verbrauchsgebundene Berechnung)
ergibt sich dieser Wert aus den Brennstoffmengen. Bei Abnehmern mit Angabe einer
Heizlast `P_max` [kW] (bedarfsgebundene Berechnung) wird der Wert über die
Volllaststunden `t_full` [h] (Gebäudedefaults oder Nutzereingabe) berechnet:

```julia
Q_total = P_max * t_full  # kWh
```

Mit dem Warmwasseranteil `wf` des Abnehmers lässt sich daraus die benötigte
Wärmemenge für Warmwasser `Q_{water}` [kWh] berechnen:

```julia
Q_water = wf * Q_total  # kWh
```

Daraus lässt sich die minimale bzw. statische Leistung `P_stat[i]` [kW], die
unabhängig von der Temperatur für jeder der 8760 Stunden im Jahr erbracht werden
muss, wie folgt berechnen:

```julia
P_stat[i] = Q_water / 8760  # kW
```

Dies ist der _statische Anteil_ der Lastkurve, der zu jeder Stunde gleich ist
(sofern es keine Betriebsunterbrechungen gibt, siehe unten). Der _dynamische Anteil_  
wird anhand der Temperaturkurve ermittelt. Dazu werden zunächst die Heizgrade
`hd[i]` [°C] für jede Stunde `i` mit Hilfe der Heizgrenztemperatur `T_lim` [°C]
und der jeweiligen Temperatur `T[i]` [°C] der Temperaturkurve berechnet. Die
gebäudespezifische Heizgrenztemperatur ist dabei die Temperatur, ab der nicht
mehr geheizt wird:

```julia
hd = zeros(length(T))
for i = 1:length(T)
    if T[i] < T_lim
        hd[i] = T_lim - T[i]
    end
end
```

Damit lässt sich dann ein Wärmebedarf pro Heizgrad `Q_hd` [kWh/°C] für den
dynamischen Anteil der Lastkurve berechnen:

```julia
Q_hd = (Q_total - Q_water) / sum(hd)
```

Der dynamische Anteil `P_dyn[i]` [kW] der Lastkurve zur Stunde `i` berechnet
sich dann zu:

```julia
P_dyn[i] = hd[i] * Q_hd
```

Und damit ist die benötigte Leistung `P[i]` zur Stunde `i` für den Abnehmer:

```julia
P[i] = P_stat[i] + P_dyn[i]
```

Das folgende Beispiel zeigt eine Berechung für einen Verluaf mit 24 Stunden:

```julia
Q_total = 1200  # kWh, total used heat
wf = 0.1        # 10%, water fraction
T_lim = 10      # °C

# temperature data
T = [-8., -8., -7., -7., -6., -4., -2.,
      0., 2., 4., 6., 7., 8., 8., 8.,
      8., 7., 6., 6., 5., 4., 2., 1., 1. ]

# calculate static load
Q_water = wf * Q_total          # kWh
P_stat = zeros(length(T))
for i = 1:length(T)
    P_stat[i] = Q_water / length(T)  # kW
end

# calculate heating degrees
T_lim = 10.0
hd = zeros(length(T))
for i = 1:length(T)
    if T[i] < T_lim
        hd[i] = T_lim - T[i]
    end
end

# calulcate dynamic & total load
Q_hd = (Q_total - Q_water) / sum(hd)
P_dyn = zeros(length(T))
P = zeros(length(T))
for i = 1:length(T)
    P_dyn[i] = hd[i] * Q_hd
    P[i] = P_stat[i] + P_dyn[i]
end
display(P)
```

Dies lässt sich dann schön mit [Gadfly](http://gadflyjl.org) darstellen:

```julia
using Gadfly

plot(
    layer(x = 1:24, y = P_stat, Geom.line,
        Theme(default_color=color("blue"))),
    layer(x = 1:24, y = P_dyn, Geom.line,
        Theme(default_color=color("orange"))),
    layer(x = 1:24, y = P, Geom.line,
        Theme(default_color=color("red"))))
```