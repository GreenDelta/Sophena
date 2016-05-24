Wärmeerzeuger
=============

Stromerzeugung
--------------
Wärmeerzeuger mit Kraft-Wärme-Kopplung (KWK-Anlagen, `Boiler.isCoGenPlant = true`) 
können zusätzlich zu Wärme auch Strom erzeugen. Die erzeugte Menge an Strom 
wird dabei wie folgt aus der erzeugten Wärmemenge berechnet:

	Volllaststunden [h] = Erzeugte Wärme [kWh] / thermische Nennleistung des Kessels [kW]
	
	Erzeugte Strommenge [kWh] = elektrische Nennleistung des Kessels [kW] * Volllaststunden [h]

