# Wärmeerzeuger

## Erzeugerlastgänge
Als Alternative zur Auswahl eines Kessels mit einer minimalen und maximalen
Leistung können für Erzeuger auch Leistungsprofile (Erzeugerlastgänge)
hinterlegt werden. Erzeuger (`Producer`) mit solchen Profilen werden über das
Attribut `hasProfile` markiert. Bei diesen Leistungsprofilen wird für jede
Stunde eine minimale und maximale Leistung angegeben. Das Script
[generate_producer_profile.py](../examples/generate_producer_profile.py)
generiert ein Beispielprofil, welches als Erzeugerlastgang importiert werden
kann. Für Erzeugerlastgänge können weder Wärmerückgewinnungen noch
Betriebsunterbrechungen angegeben werden.

## Wärmerückgewinnung
Anlagen zur Wärmerückgewinnung erhöhen die thermische Leistung eines
Wärmeerzeugers, in dem sie einen Teil der Abgaswärme nutzbar machen. In erster
Näherung kann dieser Effekt linear berechnet werden. Dazu teilt man die in den
Produktdaten angegebene Leistung des Abgaswärmetauschers `P_r` durch die
Leistung des passenden Wärmeerzeugers `P_p`, die ebenfalls in den Produktdaten
des Abgaswärmetauschers angegeben wird. Diesen Wert addiert man zu `1` und
erhält damit den Faktor der Leistungssteigerung `f_r`:

```julia
f_r = 1.0 + P_r / P_p
```

Bei den Berechnungen werden sowohl die Maximal- und Minimalleistungen (`P_bmax`
und `P_bmin`) des Wärmeerzeugers als auch der angegebene Wirkungsgrad (`eta_b`)
mit diesem Faktor multipliziert und erhöhen sich entsprechend:

```julia
P_max = P_bmax * f_r
P_min = P_bmin * f_r
eta   = eta_b  * f_r
```

**Beispiel:**

|                                                   |        |
|---------------------------------------------------|--------|
| Leistung des Wärmeaucher `P_r`                    | 100 kW |
| Leistung des passenden Erzeugers `P_p`            | 250 kW |
| Maximalleistung des Erzeugers im Projekt `P_bmax` | 200 kW |
| Minimalleistung des Erzeugers im Projekt `P_bmin` | 130 kW |
| Wirkungsgrad des Erzeugers im Projekt `eta_b`     | 35 %   |

```julia
P_r = 100
P_p = 250
P_bmax = 200
P_bmin = 130
eta_b = 0.35

f_r = 1.0 + P_r / P_p  # 1.4
P_max = P_bmax * f_r   # 280
P_min = P_bmin * f_r   # 182
eta   = eta_b  * f_r   # 0.49
```

## Minimale und maximale Leistung
Die minimale und maximale Leistung eines Wärmeerzeugers ergibt sich aus den
Produktdaten des jeweiligen Heizkessels, der dem Wärmeerzeuger zugeordnet ist.
Die Leistung erhöht sich entsprechend, wenn der Erzeuger über eine
Wärmerückgewinnung verfügt (siehe oben). Bei Erzeugerlastgängen wird die
minimale und maximale Leistung für die Simulationsrechnung für die jeweiligen
Stunden aus dem Lastgang gelesen. Zusätzlich wird bei Erzeugerlastgängen die
maximale Leistung (Nennleistung) zur Berechnung der Volllaststunden etc. durch
den Benutzer angegeben (`Producer.profileMaxPower`) bzw. beim Import eines
Erzeugerprofiles ermittelt.

## Betriebsunterbrechungen


## Volllaststunden
Die Volllaststunden (oder Vollbenutzungsstunden) `t_full` ergeben sich aus der
Menge erzeugter Wärme `Q_gen` [kWh] und der maximalen Leistung (Nennleistung)
eines Erzeugers (siehe oben) `P_max` [kW]:

```julia
t_full = Q_gen / P_max  # h
```

## Stromerzeugung
Wärmeerzeuger mit Kraft-Wärme-Kopplung (KWK-Anlagen) können zusätzlich zur Wärme
auch Strom erzeugen. Die erzeugte Menge an Strom wird dabei wie folgt aus der
erzeugten Wärmemenge berechnet:

```
Volllaststunden [h] = Erzeugte Wärme [kWh] / thermische Nennleistung des Kessels [kW]
```

```    
Strommenge [kWh] = elektrische Nennleistung des Kessels [kW] * Volllaststunden [h]
```
