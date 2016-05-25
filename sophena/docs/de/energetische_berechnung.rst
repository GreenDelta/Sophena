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

.. _Volllaststunden:

Volllaststunden
---------------
Die Volllaststunden :math:`t_{full}` [h] eines Kessels ergeben sich aus der erzeugten Wärme 
:math:`Q_{gen}` [kWh] und der thermischen Nennleistung :math:`P_{max,th}` [kW] (= maximale 
Leistung) des Kessels:

.. math::
    t_{full} = \frac{Q_{gen}}{P_{max,th}}

Die Volllaststunden entsprechen somit der Zeit in Stunden, die der Kessel bei maximaler Leistung 
betrieben werden müsste, um die gegebene Wärme zu erzeugen.

Die Hilfsfunktion ist:

.. code-block:: java 

    Producer p = ...
    double fullLoadHours = FullLoadHours.get(p, producedHeat);
    

Stromerzeugung in KWK-Anlagen
-----------------------------
Die erzeugte Menge an Strom :math:`{E_{gen}}` [kWh] wird aus den :ref:`Volllaststunden`
:math:`t_{full}` [h] und der elektrischen Nennleistung :math:`P_{max,el}` [kW] einer
KWK-Anlage berechnet:

.. math::
    E_{gen} = t_{full} * P_{max,el}


Primärenergiefaktor der Nahwärme
--------------------------------
Der Primärenergiefaktor des Wärmenetzes :math:`pef_{net}` ist eine Kennzahl, die unter den 
weiteren Ergebnissen ausgewiesem und wie folgt berechnet wird:

.. math::
    pef_{net} = \frac{ \sum_{i} {Q_{fuel,i}} * pef_{fuel,i} + (E_{use,i} - E_{gen,i}) * pef_{el} } {Q_u}

Dabei sind:

=======================  ==========================================================================
:math:`Q_{fuel,i}`       die Brennstoffenergie (Brennstoffmenge * Heizwert) für Erzeuger :math:`i` 
:math:`pef_{fuel,i}`     der Primärenergiefaktor des Brennstoffs
:math:`E_{use,i}`        Eigenstromverbrauch der Anlage
:math:`E_{gen,i}`        Stromerzeugung in der Anlage
:math:`pef_{el}`         Primärenergiefaktor von Strom
=======================  ==========================================================================

