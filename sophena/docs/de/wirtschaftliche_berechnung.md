# Funktionen der Wirtschaftlichkeitsberechnung

## Grundlegende Funktionen

### Annuitätenfaktor
Der Annuitätenfaktor `anf` dient dazu eine Zahlungsgröße in eine Reihe gleich
hoher Zahlungen pro Jahr umzuwandeln:

```julia
anf = (q - 1) / (1 - q^(-n))
```

Dabei ist `q` der Zinsfaktor (z.B. 1.02) und `n` der Betrachtungszeitraum in
Jahren ( = Projektlaufzeit, z.B. 20 Jahre).

Die Hilfsfunktion in Sophena ist (dabei wird mit `interestRate` direkt der
Zinssatz übergeben, z.B. 2%):

```java
double anf = Costs.annuitiyFactor(project, interestRate);
```

```clojure
(annuity-factor [duration interest-rate]
  (let [q (+ 1 (/ interest-rate 100))]
    (/ (- q 1) (- 1 (Math/pow q (- duration))))))
```

## Preisänderungsfaktoren
Ein Preisänderungsfaktor `pcf` beschreibt die jährliche Änderung des Preises für
ein Produkt. Steigt der Preis zum Beispiel um 2% pro Jahr, so ist der
Preisänderungsfaktor `pcf = 1.02`.

Damit lässt sich entsprechend der Preis `c_t` eines Produkts nach `t` Jahren
berechnen:

```julia
c_t = c_0 * pcf^t
```

Beispiel: Der Preis eines Produkts ist 5000 EUR und der Preisänderungsfaktor ist
1.02, so ist der Preis nach 3 Jahren:

```julia
c_3 = ((5000 * 1.02) * 1.02) * 1.02 =  5000 * 1.02^3 = 5306.04
```

## Anzahl der Wiederbeschaffungen
Ist die Lebensdauer eines Produktes $t_p$ kleiner als die Projektlaufzeit $t$,
so muss dieses Produkt entsprechend oft während der Projektlaufzeit ersetzt
werden. Die Anzahl der Wiederbeschaffungen $n$ ergibt sich zu:

$$ n = \left \lceil{\frac{t}{t_p}} \right \rceil - 1$$

Beispiel: Beträgt die Projektlaufzeit 20 Jahre und die Lebensdauer des Produkts
15 Jahre, so muss das Produkt während der Projektlaufzeit einmal ersetzt werden,
also ist in diesem Fall $n = 1$.

```java
int n = Replacements.getNumber(costResultItem, project);
```

## Barwert einer Wiederbeschaffung
Der Barwert (_present value_) der $i$-ten Wiederbeschaffung $PV_{r,i}$ nach
einer Zeit $t_{r,i}$ beschreibt den Gegenwartswert dieser Wiederbeschaffung. 
$t_{r,i}$ berechnet sich dabei aus der Lebensdauer des Produkts (in Jahren):

$$ t_{r,i} = i * t_u $$

Der Barwert der Wiederbeschaffung berechnet sich durch Abzinsen der derzeitigen
Kosten für das Produkt mit dem entsprechenden Zinsfaktor $ir$ unter Einbezug
der Preisänderung $pcf$:

$$ PV_{r,i} = \frac{C * pcf^{t_{r,i}}}{ir^{t_{r,i}}} $$

## Einbezug einer Projektförderung
Bei einer Projektförderung werden die Kapitalkosten $C$ wie folgt reduziert:

$$ C_F = C - F * af $$

Dabei ist $F$ die Fördersumme und $af$ der Annuitätenfaktor in den die
Projektlaufzeit und der Zinsatz (welcher möglicherweise bei der Förderung
anders ist) eingehen.

## Wärmegestehungskosten
Die Wärmegestehungskosten $C_Q$ [EUR/MWh] werden berechnet aus:

$$ C_Q = 1000 * \frac{C_a}{Q_{total} - Q_{net}} $$

Wobei $C_a$ die jährlichen Kosten, $Q_{total}$ die insgesamt erzeugte Wärme,
und $Q_{net}$ die Netzverluste sind (beides in [kWh]).

## Einmalige Anschlusskosten

__TODO__ derzeit nicht berücksichtigt; passt auch nicht mit der Beschreibung
-> könnte man wie bei der Projektförderung berechnen.

Die einmaligen Anschlusskosten sind die Summe aus allen Anschlussgebühren, die
von den Abnehmern gezahlt werden.  Sie werden bei der Berechnung der 
kapitalgebundenen Kosten von den zugrundegelegten Investitionskosten abgezogen.
Die angezeigten Investitionskosten bleiben unverändert.

## Ascheentsorgungskosten
Die Ascheentsorgungskosten werden zu den bedarfsgebundene Kosten eines
Wärmeerzeugers hinzugerechnet. Sie ergeben sich aus der benötigten
Brennstoffmenge, des Aschegehalts des Brennstoffs sowie den angegebenen
Ascheentsorgungskosten pro Tonne in der Brennstoffspezifikation des Erzeugers.
Die Hilfsfunktion `FuelCosts.netAshCosts` berechnet die Ascheentsorgungskosten.

Der Aschegehalt in den Brennstoffangaben bezieht sich bei Holzbrennstoffen
auf die Trockenmasse. Werden Brennstoffmengen in Schüttraummeter angegeben
werden diese entsprechend in (feuchte) Masse umgerechnet (siehe energetische
Berechnung). Dann wird die feuchte Holzmasse in die entsprechende Trockenmasse
umgerechnet und schließlich die Menge anfallender Asche berechnet. Bei allen
anderen Brennstoffen muss sichergestellt werden, dass diese immer in Tonnen
angegeben werden, da sich die Aschekosten auf Tonnen beziehen. 

