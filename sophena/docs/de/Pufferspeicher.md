# Pufferspeicher

## Kapazität eines Pufferspeichers
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

Die Klasse `BufferCapacity` berechnet diese Funktion aus einer
Heiznetzkonfiguration.

## Wärmeverlust eines Pufferspeichers
Neben Volumen sowie der maximalen und unteren Ladetemperatur kann auch der
Wärmeverlust des Pufferspeichers angegeben werden. Dieser Wert wird in der
Simulationsrechnung berücksichtig und mit 0.15% voreingestellt. Während der
Simulation wird die Kapazität des Puffers zur Stunde `i` um diesen Wert
verringert, **nachdem** alle Kessel zum Einsatz gekommen sind und dann für die
Stunde `i+1` übertragen. Außerdem wird dieser Pufferspeicherverlust für die
Stunde `i` gespeichert.

```julia
loss_factor = 0.15 / 100
buffer_load = max_buffer_capacity - buffer_capacity
buffer_loss = buffer_load * loss_factor
buffer_capacity = buffer_capacity + buffer_loss
```
