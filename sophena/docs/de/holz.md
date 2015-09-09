Heizwerte und Kosten von Holzbrennstoffen in Sophena
====================================================
Zu jedem Holzbrennstoff werden zunächst die Dichte und der Heizwert angegeben.
Die Dichte wird in Kilogramm pro Festmeter (kg/FM) angegeben. Dabei ist ein
Festmeter 1 m³ feste Holzmasse. Der Heizwert wird in kWh pro Kilogramm absolut
trockenem Holz angegeben (kWh/kg atro).

Holzmengen werden gewöhnlich in Raummeter (Ster) für Scheitholz und
Schüttraummeter für Holzhackschnitzel angegeben. Ein Ster entspricht dabei 1 m³
geschichteter Holzteile und ein Schüttraummeter 1 m³ geschütteter Holzteile.
Daneben kann die Menge von Holz in Sophena auch in Masse angegeben werden (siehe
`WoodAmountType`)

Um Holzmengen sinnvoll in Massen, Heizwerte etc. zu konvertieren braucht man 
auch immer noch den Wassergehalt (in %).

Eine Mengenangabe in Ster oder Schüttraummeter kann man mit der folgenden Formel
in Kilogramm umrechnen:

	Holzmasse = Faktor * Menge * Holzdichte / (1-Wassergehalt)
   	
Dabei ist Faktor der Umrechnungsfaktor von Ster oder Schüttraummeter in Festmeter.

