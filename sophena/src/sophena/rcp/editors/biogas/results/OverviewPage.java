package sophena.rcp.editors.biogas.results;

import java.util.ArrayList;

import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import sophena.calc.biogas.BiogasResult;
import sophena.model.Stats;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// A simple result page of a biogas plant. It shows the most important figures
/// of the calculation. More details will be added later.
class OverviewPage extends FormPage {

	private final BiogasResultEditor editor;

	OverviewPage(BiogasResultEditor editor) {
		super(editor, "sophena.OverviewPage", "Ergebnisse");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm,
			"Ergebnisse: " + editor.result().plant().name);
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);

		var section = UI.section(body, tk, "Biogas-Kennzahlen");
		var comp = UI.sectionClient(section, tk);
		UI.gridData(section, true, true);
		var table = Tables.createViewer(comp, "Name", "Wert", "Einheit");
		UI.gridData(table.getControl(), true, true);
		Tables.bindColumnWidths(table, 0.5, 0.3, 0.2);
		Tables.rightAlignColumns(table, 1);
		table.setLabelProvider(new ItemLabel());

		var items = makeItems();
		table.setInput(items);
		form.reflow(true);
	}

	private ArrayList<Item> makeItems() {
		var plant = editor.result().plant();
		var revs = editor.result().revenues();
		var items = new ArrayList<Item>();

		items.add(
			Item.of("Betriebsviertelstunden bei < 0 ct/kWh",
				Num.intStr(revs.quarterHoursBelowZero()), "BVh/a"));
		items.add(
			Item.of("Betriebsviertelstunden bei ≤ 2ct/kWh",
				Num.intStr(revs.quarterHoursBelowTwo()), "BVh/a"));
		items.add(
			Item.of("Summe der Betriebsviertelstunden",
				Num.intStr(revs.totalQuarterHours()), "BVh/a"));

		if (!plant.settings.isFixedRemuneration) {
			var item = revs.totalQuarterHours() < plant.settings.eligibleQuarterHours
				? Item.of("Förderfähige Betriebsviertelstunden", "eingehalten", "")
				: Item.errorOf("Förderfähige Betriebsviertelstunden", "nicht eingehalten");
			items.add(item);
		}

		int above85 = (int) revs.quarterHoursAbove85();
		items.add(
			Item.of("Viertelstunden >= 85% Leistung",
				Num.intStr(above85), "Vh/a"));
		if (above85 >= 4000) {
			items.add(
				Item.of("Qualitätskriterium Flexiblitätsbetrieb", "eingehalten", ""));
		} else {
			items.add(
				Item.errorOf("Qualitätskriterium Flexiblitätsbetrieb",
					"nicht eingehalten"));
		}

		items.add(Item.of("Börseneinnahmen",
			Num.intStr(revs.spotMarketRevenuesSum()), "EUR/a"));

		var netPlus = (100 - plant.settings.directMarketerShare)
			* (revs.spotMarketRevenuesSum() - revs.marketRevenuesSum())
			/ revs.feedInElectricity();
		items.add(Item.of("Nettomehrerlöse", Num.str(netPlus, 1), "ct/kWh"));

		var ratedPower = revs.feedInElectricity() / Stats.HOURS;
		items.add(Item.of("Bemessungsleistung", Num.intStr(ratedPower), "kW"));

		if (plant.settings.maxRatedPower != null) {
			var ratedItem = ratedPower > plant.settings.maxRatedPower
				? Item.errorOf("Höchstbemessungsleistung", "überschritten")
				: Item.of("Höchstbemessungsleistung", "eingehalten", "");
			items.add(ratedItem);
		}

		var stats = RuntimeStats.of(editor.result());
		items.add(Item.of("Tägliche BHKW-Start-Stops",
			Num.str(stats.avgStartCount, 1), ""));
		items.add(Item.of("Mindestdauerunterschreitungen",
			Num.intStr(stats.tooShortCount), ""));
		items.add(Item.of("Jährliche erzeugte Wärmemenge",
			Num.intStr(revs.totalGeneratedHeat()), "kWh/a"));
		return items;
	}

	private record Item(
		String name, String value, String unit, boolean isError
	) {

		static Item of(String name, String value, String unit) {
			return new Item(name, value, unit, false);
		}

		static Item errorOf(String name, String value) {
			return new Item(name, value, "", true);
		}
	}

	private static class ItemLabel extends LabelProvider
		implements ITableLabelProvider, ITableColorProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public Color getForeground(Object obj, int col) {
			if (col == 1 && obj instanceof Item item && item.isError())
				return Colors.getSystemColor(SWT.COLOR_RED);
			return null;
		}

		@Override
		public Color getBackground(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Item item))
				return null;
			return switch (col) {
				case 0 -> item.name();
				case 1 -> item.value();
				case 2 -> item.unit();
				default -> null;
			};
		}
	}

	private record RuntimeStats(double avgStartCount, int tooShortCount) {

		static RuntimeStats of(BiogasResult r) {
			if (r == null)
				return new RuntimeStats(0, 0);
			var minPhase = r.plant().minimumRuntime;

			var before = false;
			int starts = 0;
			int phase = 0;
			int tooShortCount = 0;
			for (var next : r.runtime().runFlags()) {
				if (next) {
					if (!before) {
						starts++;
					}
					phase++;
				} else {
					if (before && phase < minPhase) {
						tooShortCount++;
					}
					phase = 0;
				}
				before = next;
			}

			return new RuntimeStats(((double) starts) / 365.0, tooShortCount);
		}
	}
}
