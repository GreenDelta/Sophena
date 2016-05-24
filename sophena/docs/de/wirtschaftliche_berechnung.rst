Funktionen der Wirtschaftlichkeitsberechnung
============================================

Annuitätenfaktor
----------------
Der Annuitätenfaktor :math:`anf` dient dazu eine Zahlungsgröße in eine Reihe
gleich hoher Zahlungen pro Jahr umzuwandeln:

.. math::
   anf = \frac{q - 1}{1 - q^{-n}}

Dabei ist :math:`q` der Zinsfaktor (z.B. 1.02) und :math:`n` der 
Betrachtungszeitraum in Jahren ( = Projektlaufzeit, z.B. 20 Jahre).

Die Hilfsfunktion ist:

.. code-block:: java

   double anf = AnnuitiyFactor.get(1.02, 20);
   
Für Projekte wird allerdings direkt der Zinssatz aus den Kostendaten übergeben 
(z.B. 2%):

.. code-block:: java

   double anf = AnnuitiyFactor.get(project, 2);