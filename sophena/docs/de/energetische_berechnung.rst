Funktionen der energetischen Berechnung
=======================================

TODO: FuelEnergyDemand: benötigte Brennstoffenergie, je nach Erzeugertyp

.. _Heizwert:

Heizwert
--------
Alle Brennstoffe werden in einer Referenzeinheit :math:`u` angegeben (z.B. m3 für Erdgas). 
Der Heizwert :math:`cv` [kWh/u] entspricht der Menge Wärme :math:`Q_{gen,u}` [kWh], die pro 
Referenzeinheit des Brennstoffs erzeugt werden können:

.. math::
    cv = \frac{Q_{gen,u}}{u}

Für Holzbrennstoffe wird der Heizwert pro Kilogramm absolut trockener Masse (kg atro) 
angegeben [kWh/kg atro].


.. _Brennstoffenergie:

Brennstoffenergie
-----------------
Die Wärme :math:`Q` [kWh], die durch die Verbrennung einer Menge :math:`a` eines Brennstoffs 
erzeugt werden kann, wird wie folgt berechnet:

.. math::
    Q = a * cv

Die Brennstoffmenge :math:`a` wird in der Referenzeinheit des Brennstoffs angegeben. 
:math:`cv` ist der :ref:`Heizwert` des Brennstoffs in kWh pro Referenzeinheit.
