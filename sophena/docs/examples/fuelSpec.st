fuel := SoFuel new.
fuel
	label: 'Fichte';
	density: 379;
	calorificValue: 5200;
	co2Emissions: 44.3;
	primaryEnergyFactor: 0.2;
	ashContent: 1.

spec := SoFuelSpec new.
spec fuel: fuel;
	waterContent: 30;
	pricePerUnit: 10;
	woodAmountType: (SoWoodAmountType chips);
	taxRate: 19;
	ashCosts: 560.
