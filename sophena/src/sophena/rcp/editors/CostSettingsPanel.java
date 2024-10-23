package sophena.rcp.editors;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.FuelDao;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FundingType;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.Texts.TextDispatch;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class CostSettingsPanel {

	public boolean isForProject;

	private final Editor editor;
	private final Supplier<CostSettings> fn;
	private FormToolkit tk;
	private Composite inner;
	private GridData dataInner;
	private final Runnable updateScrolledForm;

	public CostSettingsPanel(Editor editor, Supplier<CostSettings> fn, Runnable updateScrolledForm) {
		this.editor = editor;
		this.fn = fn;
		this.updateScrolledForm = updateScrolledForm;
	}

	private CostSettings costs() {
		return fn.get();
	}

	public void render(FormToolkit tk, Composite body) {
		this.tk = tk;
		if (costs() != null) {
			generalSection(body);
			financeSection(body);
			createOtherSection(body);
			if (isForProject) {
				AnnualCostsTable
						.on(editor, fn)
						.render(body, tk);
			}
			createPriceChangeSection(body);
		}
	}

	private void generalSection(Composite body) {
		Composite c = UI.formSection(body, tk, "Allgemein");
		UI.gridLayout(c, 4);

		t(c, "Mittlerer Stundenlohn", "EUR", costs().hourlyWage)
				.onChanged(s -> costs().hourlyWage = Num.read(s));
		UI.filler(c);

		t(c, "Strompreis", "EUR/kWh", costs().electricityPrice)
				.onChanged(s -> costs().electricityPrice = Num.read(s));
		UI.filler(c);

		t(c, "Eigenstrombedarf", "%", costs().electricityDemandShare)
				.onChanged(s -> costs().electricityDemandShare = Num.read(s));
		HelpLink.create(c, tk, "Eigenstrombedarf",
				H.ElectricityDemandShare);
		EntityCombo<Fuel> combo = new EntityCombo<>();
		combo.create("Verbrauchter Strom", c, tk);
		UI.gridData(combo.getControl(), false, false).widthHint = 235;
		List<Fuel> fuels = new FuelDao(App.getDb())
				.getAll().stream()
				.filter(f -> f.group == FuelGroup.ELECTRICITY)
				.sorted(Sorters.byName())
				.collect(Collectors.toList());
		combo.setInput(fuels);
		combo.select(costs().projectElectricityMix);
		combo.onSelect(e -> {
			costs().projectElectricityMix = e;
			editor.setDirty();
		});
		UI.filler(c);
		UI.filler(c);

		if (isForProject) {
			String heatRevenues = costs().heatRevenues == 0
					? "0"
					: Num.str(costs().heatRevenues, 2);
			t(c, "Mittlere Wärmeerlöse", "EUR/MWh", heatRevenues).onChanged(
					s -> costs().heatRevenues = Num.read(s));
			UI.filler(c);
			t(c, "Mittlere Stromerlöse", "EUR/kWh",
					Num.str(costs().electricityRevenues, 4)).onChanged(
							s -> costs().electricityRevenues = Num.read(s));
			HelpLink.create(c, tk, "Mittlere Stromerlöse",
					H.ElectricityRevenues);
		}
	}

	private void financeSection(Composite body) {
		Composite c = UI.formSection(body, tk, "Finanzierung");
		UI.gridLayout(c, 3);
		t(c, "Kapital-Mischzinssatz (ohne Förderung)", "%",
				costs().interestRate)
						.onChanged(s -> costs().interestRate = Num.read(s));

		t(c, "Kapital-Mischzinssatz (mit Förderung)", "%",
				costs().interestRateFunding)
						.onChanged(s -> costs().interestRateFunding = Num
								.read(s));
		if (isForProject) {
			t(c, "Einmalige Anschlusskosten", "EUR", costs().connectionFees)
			.onChanged(s -> costs().connectionFees = Num.read(s));
			t(c, "Investitionsförderung absolut", "EUR", costs().funding)
					.onChanged(s -> costs().funding = Num.read(s));					
			t(c, "Investitionsförderung prozentual", "%",
					costs().fundingPercent).onChanged(
							s ->{
								costs().fundingPercent = Num.read(s);	
								setInnerGridVisible(costs().fundingPercent > 0);
							});
			if(costs().fundingPercent == 0)
				costs().fundingTypes = FundingType.BiomassBoiler.getValue() | FundingType.SolarThermalPlant.getValue() | FundingType.BoilerAccessories.getValue() | FundingType.HeatRecovery.getValue() | FundingType.FlueGasCleaning.getValue() | FundingType.BufferTank.getValue() 
				| FundingType.BoilerHouseTechnology.getValue() | FundingType.Building.getValue() | FundingType.Pipe.getValue() | FundingType.HeatingNetTechnology.getValue() | FundingType.HeatingNetConstruction.getValue() | FundingType.TransferStation.getValue() | FundingType.Planning.getValue();
			createCheckSection(body);
			setInnerGridVisible(costs().fundingPercent > 0);
		}
	}

	private void createCheckSection(Composite c)
	{
		inner = tk.createComposite(c);
		UI.gridLayout(inner, 4);	
		dataInner = new GridData(SWT.FILL, SWT.FILL, false, false);
	    inner.setLayoutData(dataInner);
	    setCheckBox(tk.createButton(inner, M.BiomassBoiler, SWT.CHECK), FundingType.BiomassBoiler.getValue());
	    setCheckBox(tk.createButton(inner, M.FossilFuelBoiler, SWT.CHECK), FundingType.FossilFuelBoiler.getValue());
	    setCheckBox(tk.createButton(inner, M.CogenerationPlant, SWT.CHECK), FundingType.CogenerationPlant.getValue());
	    setCheckBox(tk.createButton(inner, M.SolarThermalPlant, SWT.CHECK), FundingType.SolarThermalPlant.getValue());
	    setCheckBox(tk.createButton(inner, M.ElectricHeatGenerator, SWT.CHECK), FundingType.ElectricHeatGenerator.getValue());
	    setCheckBox(tk.createButton(inner, M.OtherHeatSource, SWT.CHECK), FundingType.OtherHeatSource.getValue());
	    setCheckBox(tk.createButton(inner, M.BoilerAccessories, SWT.CHECK), FundingType.BoilerAccessories.getValue());
	    setCheckBox(tk.createButton(inner, M.OtherEquipment, SWT.CHECK), FundingType.OtherEquipment.getValue());
	    setCheckBox(tk.createButton(inner, M.HeatRecovery, SWT.CHECK), FundingType.HeatRecovery.getValue());
	    setCheckBox(tk.createButton(inner, M.FlueGasCleaning, SWT.CHECK), FundingType.FlueGasCleaning.getValue());
	    setCheckBox(tk.createButton(inner, M.BufferTank, SWT.CHECK), FundingType.BufferTank.getValue());
	    setCheckBox(tk.createButton(inner, M.BoilerHouseTechnology, SWT.CHECK), FundingType.BoilerHouseTechnology.getValue());
	    setCheckBox(tk.createButton(inner, M.Building, SWT.CHECK), FundingType.Building.getValue());
	    setCheckBox(tk.createButton(inner, M.Pipe, SWT.CHECK), FundingType.Pipe.getValue());
	    setCheckBox(tk.createButton(inner, M.HeatingNetTechnology, SWT.CHECK), FundingType.HeatingNetTechnology.getValue());
	    setCheckBox(tk.createButton(inner, M.HeatingNetConstruction, SWT.CHECK), FundingType.HeatingNetConstruction.getValue());
	    setCheckBox(tk.createButton(inner, M.TransferStation, SWT.CHECK), FundingType.TransferStation.getValue());
	    setCheckBox(tk.createButton(inner, M.Planning, SWT.CHECK), FundingType.Planning.getValue());
	    setCheckBox(tk.createButton(inner, M.HeatPump, SWT.CHECK), FundingType.HeatPump.getValue());
	}
	
	private void setCheckBox(Button check, int fundingTypeValue)
	{
		check.setSelection((costs().fundingTypes & fundingTypeValue) > 0);
		Controls.onSelect(check, (e) -> {
			if(check.getSelection())
				costs().fundingTypes |= fundingTypeValue;
			else
				costs().fundingTypes &= ~fundingTypeValue;
			editor.setDirty();
		});
	}
	
	private void setInnerGridVisible(Boolean visible)
	{
		inner.setVisible(visible);
		dataInner.exclude = !visible;	
		inner.requestLayout();
		
		updateScrolledForm.run();
	}
	
	private void createOtherSection(Composite body) {
		Composite c = UI.formSection(body, tk, "Sonstige Kosten");
		UI.gridLayout(c, 3);
		t(c, "Versicherung", "%", costs().insuranceShare)
				.onChanged(s -> costs().insuranceShare = Num.read(s));
		t(c, "Sonstige Abgaben (Steuern, Pacht, …)", "%", costs().otherShare)
				.onChanged(s -> costs().otherShare = Num.read(s));
		t(c, "Verwaltung", "%", costs().administrationShare)
				.onChanged(s -> costs().administrationShare = Num.read(s));
	}

	private void createPriceChangeSection(Composite body) {
		Composite c = UI.formSection(body, tk, "Preisänderungsfaktoren");
		UI.gridLayout(c, 3);
		t(c, "Investitionen", "", costs().investmentFactor)
				.onChanged(s -> costs().investmentFactor = Num.read(s));
		t(c, "Biomasse-Brennstoff", "", costs().bioFuelFactor)
				.onChanged(s -> costs().bioFuelFactor = Num.read(s));
		t(c, "Fossiler Brennstoff", "", costs().fossilFuelFactor)
				.onChanged(s -> costs().fossilFuelFactor = Num.read(s));
		t(c, "Strom", "", costs().electricityFactor)
				.onChanged(s -> costs().electricityFactor = Num.read(s));
		t(c, "Lohnkosten und sonstige Kosten", "", costs().operationFactor)
				.onChanged(s -> costs().operationFactor = Num.read(s));
		t(c, "Instandhaltung", "", costs().maintenanceFactor)
				.onChanged(s -> costs().maintenanceFactor = Num.read(s));
		t(c, "Wärmeerlöse", "", costs().heatRevenuesFactor)
				.onChanged(s -> costs().heatRevenuesFactor = Num.read(s));
		t(c, "Stromerlöse", "", costs().electricityRevenuesFactor)
				.onChanged(
						s -> costs().electricityRevenuesFactor = Num.read(s));
	}

	private TextDispatch t(Composite comp, String label, String unit,
			double initial) {
		return t(comp, label, unit, Num.str(initial));
	}

	private TextDispatch t(Composite comp, String label, String unit,
			String initial) {
		Label lab = UI.formLabel(comp, tk, label);
		UI.gridData(lab, false, false).widthHint = 250;
		Text text = tk.createText(comp, null, SWT.BORDER);
		UI.gridData(text, false, false).widthHint = 250;
		UI.formLabel(comp, tk, unit);
		return Texts.on(text)
				.required()
				.decimal()
				.init(initial)
				.onChanged(s -> editor.setDirty());
	}

}
