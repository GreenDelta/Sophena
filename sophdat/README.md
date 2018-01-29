#sophdat
sophdat ist das Datenpaketierungstool für Sophena. Die Produkt- und Klimadaten
in Sophena sind nicht Bestandteil des Sophena-Quellcodes und werden unter
anderen Lizenzbedingungen verteilt.

sophdat konvertiert die gelieferten Daten zunächst in ein portables CSV-Format.
Die Daten werden uns in einer Excel-Datei mit definiertem Format geliefert. Das
Script `xls2csv.py` wandelt die Excel-Datei in die entsprechenden CSV-Dateien
um.

## CSV Format

### Hersteller 
Zur Zeit werden noch keine Herstellerinformationen separat geliefert. Wir 
extrahieren daher nur alle Herstellernamen und generieren die Herstellertabelle
damit:

```
manufacturers.csv
0. id
1. name
2. address
3. url
4. description
```

### 

### Biomassekessel
0.  Id
1.  Produktbereich
2.  Produktgruppe
3.  Hersteller
4.  Bezeichnung
5.  Link
6.  Preis
7.  Brennstoff
8.  Maximale Leistung
9.  Minimale Leistung
10. Wirkungsgrad
11. Zusatzinformation

##FossilerKessel
0.  Id
1.  Produktbereich
2.  Produktgruppe
3.  Hersteller
4.  Bezeichnung
5.  Link
6.  Preis
7.  Brennstoff
8.  Maximale Leistung
9.  Minimale Leistung
10. Wirkungsgrad
11. Zusatzinformation

##KWK-Anlage
0.  Id
1.  Produktbereich
2.  Produktgruppe
3.  Hersteller
4.  Bezeichnung
5.  Link
6.  Preis
7.  Brennstoff
8.  Maximale Leistung (thermisch)
9.  Minimale Leistung (thermisch)
10. Wirkungsgrad (thermisch)
11. Maximale Leistung (elektrisch)
12. Minimale Leistung (elektrisch)
13. Wirkungsgrad (elektrisch)
14. Zusatzinformation

##Wärmerückgewinnung
0.	id
1.	Produktbereich
2.	Produktgruppe
3.	Hersteller
4.	Bezeichnung
5.	Link
6.	Preis
7.	Leistung
8.	Art_Waermeerzeuger
9.	Brennstoff_Waermeerzeuger
10.	Leistung_Waermeerzeuger
11.	Zusatzinformation

##Rauchgasreinigung
0.	id
1.	Produktbereich
2.	Produktgruppe
3.	Hersteller
4.	Bezeichnung
5.	Link
6.	Preis
7.	Max_Volumenstrom
8.	Brennstoff_Waermeerzeuger
9.	Max_Leistung_Waermeerzeuger
10.	Eigenstrombedarf
11.	Art_Reinigung
12.	Typ_Reinigung
13.	Max_Abscheidegrad
14.	Zusatzinformation

##PufferSpeicher
0.	id
1.	Produktbereich
2.	Produktgruppe
3.	Hersteller
4.	Bezeichnung
5.	Link
6.	Preis
7.	Volumen
8.	Durchmesser
9.	Höhe
10.	Isolierung
11.	Zusatzinformation

##Wärmeleitungen
0.	id
1.	Produktbereich
2.	Produktgruppe
3.	Hersteller
4.	Bezeichnung
5.	Link
6.	Preis
7.	Material
8.	Art
9.	U_Wert
10.	Innendurchmesser_Medienrohr
11.	Aussendurchmesser_Medienrohr
12.	Aussendurchmesser_Gesamt
13.	Lieferausfuehrung
14.	Max_Temperatur
15.	Max_Druck
16.	Zusatzinformation

##Hausübergabestationen
0.	id
1.	Produktbereich
2.	Produktgruppe
3.	Hersteller
4.	Bezeichnung
5.	Link
6.	Preis
7.	Gebaeudetyp
8.	Leistung
9.	Art_Waermetauscher
10.	Material_Waermetauscher
11.	Warmwasser
12.	Regelung
13.	Zusatzinformation

##fuels
0. id
1. name
2. unit
3. calorificValue
4. wood
5. density
6. co2Emissions
7. primaryEnergyFactor

## building_states
0. id
1. name
2. type
3. heatingLimit
4. waterFraction
5. loadHours
6. isDefault
7. index

### product_groups
0. id
1. index
2. type
3. name
4. duration
5. repair
6. maintenance
7. operation