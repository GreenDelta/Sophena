package sophena.rcp.editors;

import java.util.function.Supplier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.CostSettings;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.Texts.TextDispatch;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class CostSettingsPanel {

	public boolean isForProject;

	private final Editor editor;
	private final Supplier<CostSettings> fn;
	private FormToolkit tk;

	public CostSettingsPanel(Editor editor, Supplier<CostSettings> fn) {
		this.editor = editor;
		this.fn = fn;
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
			t(c, "Investitionsförderung allgemein", "EUR", costs().funding)
					.onChanged(s -> costs().funding = Num.read(s));
			t(c, "Förderung Biomassekessel", "EUR/kW",
					costs().fundingBiomassBoilers).onChanged(
							s -> costs().fundingBiomassBoilers = Num.read(s));
			t(c, "Förderung Wärmenetz", "EUR/m",
					costs().fundingHeatNet).onChanged(
							s -> costs().fundingHeatNet = Num.read(s));
			t(c, "Förderung Hausübergabestationen", "EUR/Stk.",
					costs().fundingTransferStations).onChanged(
							s -> costs().fundingTransferStations = Num.read(s));
			t(c, "Einmalige Anschlusskosten", "EUR", costs().connectionFees)
					.onChanged(s -> costs().connectionFees = Num.read(s));
		}
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
