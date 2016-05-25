Jahresdauerlinie einzelner Abnehmer
===================================

In die Berechnung der Jahresdauerlinie oder Lastkurve für einzelne Abnehmer geht zunächst
die benötigte Wärme :math:`Q_{total}` [kWh] des Abnehmers für das gesamte Jahr ein. Bei
Abnehmern mit Brennstoffangaben (verbrauchsgebundene Berechnung) ergibt sich dieser Wert 
aus den Brennstoffmengen. Bei Abnehmern mit Angabe der Heizlast :math:`P_{max}` [kW] 
(bedarfsgebundene Berechnung) wird der Wert mit Hilfe der :ref:`Volllaststunden` 
:math:`t_{full}` [h] (Gebäudedefaults oder Nutzereingabe) berechnet:

.. math::
    Q_{total} = P_{max} * t_{full}

Mit dem Warmwasseranteil :math:`wf` des Abnehmers lässt sich daraus die benötigte Wärmemenge 
für Warmwasser :math:`Q_{water}` [kWh] berechnen:

.. math::
    Q_{water} = wf * Q_{total}

Daraus lässt sich die minimale Leistung :math:`P_{min}` [kW], die unabhängig von der Temperatur 
für die 8760 Stunden im Jahr erbracht werden muss, wie folgt berechnen:

.. math::
    P_{min} = \frac{Q_{water}}{8760}

Dies ist der statische Anteil der Lastkurve, der zu jeder Stunde gleich ist. Der dynamische Anteil 
wird anhand der Temperaturkurve ermittelt. Dazu werden zunächst die Heizgrade :math:`hd_i` [°C] für 
jede Stunde :math:`i` mit Hilfe der Heizgrenztemperatur :math:`T_{lim}` [°C] und der jeweiligen 
Temperatur :math:`T_i` [°C] der Temperaturkurve berechnet. Die gebäudespezifische Heizgrenztemperatur ist 
dabei die Temperatur, ab der nicht mehr geheizt wird:

.. math::
    hd_i = \left\{\begin{matrix}
            & 0 & \text{if} & T_i >= T_{lim} \\
            & T_{lim} - T_i & \text{else} \\
            \end{matrix}\right.

Damit lässt sich dann ein Wärmebedarf pro Heizgrad :math:`Qd` [kWh/°C] für den dynamischen Anteil der 
Lastkurve berechnen:

.. math:: 
    Qd = \frac{Q_{total} - Q_{water}}{\sum{hd_i}}

Der dynamische Anteil :math:`P_{dyn,i}` [kW] der Lastkurve zur Stunde :math:`i` berechnet sich dann
zu:

.. math::
    P_{dyn,i} = hd_i * Qd

Und damit ist die benötigte Leistung zur Stunde :math:`i` für den Abnehmer:

.. math::
    P_i = P_{min} + P_{dyn,i}
