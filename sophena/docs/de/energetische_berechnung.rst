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
Die Wärmemenge :math:`Q_{fuel}` [kWh], die durch die Verbrennung einer Menge :math:`a` 
eines Brennstoffs erzeugt werden kann, wird wie folgt berechnet:

.. math::
    Q_{fuel} = a * cv

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
    

.. _Nutzungsgrad:

Nutzungsgrad
------------
Der Nutzungsgrad :math:`ur` ist allgemein das Verhältnis aus nutzbar gemachter Energie zu zugeführter 
Energie und wird aus dem Wirkungsgrad :math:`er` und dem Bereitschaftswirkungsgrad :math:`sr` berechnet:

.. math::
    ur = er * sr

Der Wirkungsgrad des Kessels ist eine Herstellerangabe. Der Bereitschaftswirkungsgrad :math:`sr` wird
wie folgt berechnet:

.. math::
    sr = \frac{1}{(\frac{t_u}{t_{full}}-1)*sl + 1}

Dabei sind :math:`t_u` [h] die Nutzungsdauer (gewöhnlich 8760 Stunden) des Kessels, :math:`t_{full}` [h] die
:ref:`Volllaststunden` und :math:`sl` der spezifische Bereitschaftsverlust. Als spezifischer 
Bereitschaftsverlust wird in Sophena immer ein empirischer Wert für kleine Kessel von 0.014 angenommen. 

.. code-block:: java

    double ur = UtilisationRate
                .ofSmallBoiler()
                .efficiencyRate(0.9)
                .usageDuration_h(8760)
                .fullLoadHours_h(2000)
                .get();


.. _Stromerzeugung:

Stromerzeugung
--------------
Die erzeugte Menge an Strom :math:`{E_{gen}}` [kWh] wird aus den :ref:`Volllaststunden`
:math:`t_{full}` [h] und der elektrischen Nennleistung :math:`P_{max,el}` [kW] einer
KWK-Anlage berechnet:

.. math::
    E_{gen} = t_{full} * P_{max,el}

Da die Volllaststunden aus der erzeugten Wärme berechnet werden, sieht die Hilfsfunktion so
aus:

.. code-block:: java

    Producer p = ...
    double generatedElectricity = GeneratedElectricity.get(p, generatedHeat);


.. _Eigenstrombedarf:

Eigenstrombedarf
----------------
Der Eigenstrombedarf wird nicht aus den Daten des ausgewählten Kessels berechnet, da 
eine vernünftige Abschätzung auf Basis der dort angegebenen elektrischen Anschlussleistung 
sehr schwierig ist. Stattdessen wird dafür bei den allgemeinen Angaben eine Kennzahl 
angegeben, diese %-Angabe bezieht sich auf die im Heizhaus erzeugte Wärmemenge, als Default 
werden 1,5 % angegeben. Werden also z.B. 2000 MWh Wärme pro Jahr erzeugt, so würde im 
Defaultfall der Eigenstrombedarf mit 2000 * 0,015 = 30 MWh = 30.000 kWh abgeschätzt werden.

Der Anteil wird derzeit in den `CostSettings` eines Projekts gespeichert. Die Hilfsfunktion
zur Berechnung des Eigenstrombedarfs sieht entsprechend so aus:

.. code-block:: java

    double usedElectricity = UsedElectricity.get(producedHeat, costSettings);


.. _GenutzteWaerme:

Genutzte Wärme
--------------
Die Genutze Wärme ist die erzeugte Wärme insgesamt abzüglich der Verteilungsverluste im Netz.

Die Hilfsfunktion dafür ist:

.. code-block:: java

    double usedHeat = UsedHeat.get(projectResult);


Primärenergiefaktor der Nahwärme
--------------------------------
Der Primärenergiefaktor des Wärmenetzes :math:`pef_{net}` ist eine Kennzahl, die unter den 
weiteren Ergebnissen ausgewiesem und wie folgt berechnet wird:

.. math::
    pef_{net} = \frac{ \sum_{i} {Q_{fuel,i}} * pef_{fuel,i} + (E_{use,i} - E_{gen,i}) * pef_{el} } {Q_u}

Dabei sind:

=======================  ==========================================================================
:math:`Q_{fuel,i}`       die :ref:`Brennstoffenergie` für Erzeuger :math:`i` 
:math:`pef_{fuel,i}`     der Primärenergiefaktor des Brennstoffs
:math:`E_{use,i}`        :ref:`Eigenstrombedarf` des Erzeugers
:math:`E_{gen,i}`        :ref:`Stromerzeugung` in der Anlage
:math:`pef_{el}`         Primärenergiefaktor von Strom
:math:`Q_u`              :ref:`GenutzteWaerme`
=======================  ==========================================================================

Die Hilfsfunktion dafür ist:

.. code-block:: java

    double pef = PrimaryEnergyFactor.get(project, projectResult);