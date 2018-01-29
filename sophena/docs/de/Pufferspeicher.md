# Pufferspeicher

Die Kapazität `Q` eines Pufferspeichers wird wie folgt berechnet:

```julia
Q = c * m * delta_T
```

Dabei ist `c` die spezifische Wärmekapazität von Wasser (1.166 Wh/kg K), `m` die
Masse des Wassers und `delta_T` der Temperaturunterschied zwischen der maximalen
und der unteren Pufferladetemperatur. Die untere Pufferladetemperatur ist dabei
per default die Rücklauftemperatur des Netzes, kann aber vom Nutzer
überschrieben werden. Ein Pufferspeicher mit 10.000l Volumen hat also bei einer
maximalen Ladetemperatur von 80 °C und unteren Ladetemperatur von 60 °C eine
Kapazität von 233.2 kWh.

```julia
c = 0.001166  # kWh/(kg K) = kWh/(L K)
m = 10000     # kg = L
Q = c * m * (80 - 60)  # kWh
```

