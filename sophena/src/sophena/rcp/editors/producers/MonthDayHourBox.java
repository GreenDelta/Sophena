package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.HoursTrace;
import sophena.model.MonthDayHour;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.UI;

class MonthDayHourBox {

	private final String[] MONTHS = {
			"Januar", "Februar", "März", "April", "Mai", "Juni", "Juli",
			"August", "September", "Oktober", "November", "Dezember" };

	private Combo dayCombo;
	private Combo monthCombo;
	private Combo hourCombo;

	MonthDayHourBox(String label, Composite parent) {
		this(label, parent, null);
	}

	MonthDayHourBox(String label, Composite parent, FormToolkit tk) {
		Composite c = tk != null ? tk.createComposite(parent)
				: new Composite(parent, SWT.NONE);
		UI.innerGrid(c, 4);
		if (tk != null) {
			tk.createLabel(c, label);
		} else {
			Label lab = new Label(c, SWT.NONE);
			GridData gd = new GridData();
			// label with same width
			gd.widthHint = 50;
			lab.setLayoutData(gd);
			lab.setText(label);
		}
		dayCombo = createCombo(c, 30, getDayItems(0));
		monthCombo = createCombo(c, 70, MONTHS);
		hourCombo = createCombo(c, 50, getHourItems());
		if (tk != null) {
			tk.adapt(dayCombo);
			tk.adapt(monthCombo);
			tk.adapt(hourCombo);
		}
		Controls.onSelect(monthCombo, (e) -> monthChanged());
	}

	private void monthChanged() {
		int monthIdx = monthCombo.getSelectionIndex();
		int dayIdx = dayCombo.getSelectionIndex();
		String[] newDayItems = getDayItems(monthIdx);
		dayCombo.setItems(newDayItems);
		// select a day that is equal or closest to the old selected day
		while (dayIdx >= newDayItems.length)
			dayIdx--;
		dayCombo.select(dayIdx);
	}

	void select(MonthDayHour mdh) {
		if (mdh == null)
			return;
		monthCombo.select(mdh.getMonth() - 1);
		dayCombo.select(mdh.getDay() - 1);
		hourCombo.select(mdh.getHour());
	}

	MonthDayHour getSelection() {
		return MonthDayHour.of(
				monthCombo.getSelectionIndex() + 1,
				dayCombo.getSelectionIndex() + 1,
				hourCombo.getSelectionIndex());
	}

	private Combo createCombo(Composite c, int width, String[] items) {
		Combo combo = new Combo(c, SWT.READ_ONLY | SWT.BORDER | SWT.H_SCROLL);
		GridData gridData = new GridData();
		gridData.widthHint = width;
		gridData.minimumWidth = width;
		combo.setLayoutData(gridData);
		combo.setItems(items);
		combo.select(0);
		return combo;
	}

	private String[] getDayItems(int monthIndex) {
		if (monthIndex < 0 || monthIndex > 11)
			return new String[0];
		int count = HoursTrace.DAYS_IN_MONTH[monthIndex];
		String[] items = new String[count];
		for (int i = 0; i < count; i++)
			items[i] = Integer.toString(i + 1).concat(".");
		return items;
	}

	private String[] getHourItems() {
		String[] items = new String[24];
		for (int i = 0; i < 24; i++)
			items[i] = Integer.toString(i).concat(" Uhr");
		return items;
	}
}
