as yet unclassified
massFactor: aSymbol
	"Return the factor for converting the given wood amount type to mass."
	(aSymbol = (self chips)) ifTrue: [ ^ 0.4 ].
	(aSymbol = (self logs)) ifTrue: [ ^ 0.7 ].	
	^ 1.0