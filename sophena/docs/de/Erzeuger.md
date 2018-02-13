# Wärmeerzeuger

## Erzeugerlastgänge
Als Alternative zur Auswahl eines Kessels mit einer minimalen und maximalen
Leistung können für Erzeuger auch Leistungsprofile (Erzeugerlastgänge)
hinterlegt werden. Bei diesen Leistungsprofilen wird für jede Stunde eine
minimale und maximale Leistung angegeben. Erzeuger mit solchen Profilen werden
über das Attribut `hasProfile` markiert.

## Stromerzeugung
Wärmeerzeuger mit Kraft-Wärme-Kopplung (KWK-Anlagen,
`Boiler.isCoGenPlant = true`) können zusätzlich zu Wärme auch Strom erzeugen.
Die erzeugte Menge an Strom wird dabei wie folgt aus der erzeugten Wärmemenge
berechnet:

```
Volllaststunden [h] = Erzeugte Wärme [kWh] / thermische Nennleistung des Kessels [kW]
```

```    
Strommenge [kWh] = elektrische Nennleistung des Kessels [kW] * Volllaststunden [h]
```
