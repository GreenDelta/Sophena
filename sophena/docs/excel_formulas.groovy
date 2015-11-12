// Beautifier: http://excelformulabeautifier.com/
// Latex-Editor: http://www.codecogs.com/latex/eqneditor.php

def reps = [

   '(\\$)?D(\\$)?10': 'Zinssatz',
    '(\\$)?D(\\$)?11': 'ZinssatzFörderung',
    '(\\$)?D(\\$)?12': 'Projektlaufzeit',
    '(\\$)?D(\\$)?13': 'PreisFaktorInvestition',
    '(\\$)?D(\\$)?14': 'Investitionsförderung',
    '(\\$)?D(\\$)?15': 'jährlicheStromerlöse',
    
    '(\\$)?D(\\$)?17': 'Annuitätenfaktor',
    '(\\$)?D(\\$)?18': 'BarwertfaktorBetriebsKosten',
    '(\\$)?D(\\$)?19': 'BarwertfaktorInstandhaltung',
    
    '(\\$)?Y(\\$)?9': 'Mehrwertsteuersatz',
    '(\\$)?Y(\\$)?10': 'PreisFaktorBiomasseBrennstoff',
    '(\\$)?Y(\\$)?11': 'PreisFaktorFossilerBrennstoff',
    '(\\$)?Y(\\$)?12': 'StrompreisNetto',
    '(\\$)?Y(\\$)?13': 'PreisFaktorStrom',
    '(\\$)?Y(\\$)?14': 'PreisFaktorBetriebsKosten',
    '(\\$)?Y(\\$)?15': 'PreisFaktorInstandhaltung',
    '(\\$)?Y(\\$)?16': 'Stundenlohn',
    '(\\$)?Y(\\$)?17': 'Versicherung',
    '(\\$)?Y(\\$)?18': 'SonstigeAbgaben',
    '(\\$)?Y(\\$)?19': 'Verwaltung',
    
    'C[2-9][0-9]': 'Investitionskosten',
    'D[2-9][0-9]': 'Nutzungsdauer',
    'E[2-9][0-9]': 'Instandsetzung',
    'F[2-9][0-9]': 'WartungInspektion',
    'G[2-9][0-9]': 'AufwandBedienen',
    'H[2-9][0-9]': 'AnzahlErsatz',
    'I[2-9][0-9]': 'Barwert1.Ersatz',
    'J[2-9][0-9]': 'Barwert2.Ersatz',
    'K[2-9][0-9]': 'Barwert3.Ersatz',
    'L[2-9][0-9]': 'Barwert4.Ersatz',
    'M[2-9][0-9]': 'Barwert5.Ersatz',
    'N[2-9][0-9]': 'Restwert',
    'O[2-9][0-9]': 'PreisFaktorBedarfsgebundeneKosten',
    'P[2-9][0-9]': 'BarwertfaktorBedarfsgebundeneKosten',
    'Q[2-9][0-9]': 'KapitalgebundeneKosten',
    'R[2-9][0-9]': 'Brennstoff',
    'S[2-9][0-9]': 'BrennstoffenergieAnlage',
    'T[2-9][0-9]': 'Brennstoffpreis',
    'U[2-9][0-9]': 'MehrwertsteuersatzBrennstoff',
    'V[2-9][0-9]': 'Eigenstrombedarf',
    'W[2-9][0-9]': 'BedarfsgebundeneKosten',
    'X[2-9][0-9]': 'BetriebsgebundeneKosten',
    'Y[2-9][0-9]': 'BetriebsgebundeneKostenMwSt'
]

def txt = '''
=G71*$Y$16*$D$17*$D$18+C71*(E71+F71)*$D$17*$D$19
'''

reps.each { key, val ->

    txt = txt.replaceAll(key, val)

}

println txt