package sophena.rcp.editors.producers;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.HoursTrace;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.UI;

class MonthDayHourBox {

	private final String[] MONTHS = {
			"Januar", "Februar", "März", "April", "Mai", "Juni", "Juli",
			"August", "September", "Oktober", "November", "Dezember" };

	private List<Consumer<MonthDay>> listeners = new ArrayList<>();
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
		Controls.onSelect(dayCombo, (e) -> fireChange());
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
		fireChange();
	}

	private void fireChange() {
		String value = getSelection();
		if (value == null)
			return;
		String[] dates = value.split("-");
		if (dates.length != 3)
			return;
		try {
			MonthDay monthDay = MonthDay.of(Integer.valueOf(dates[0]),
					Integer.valueOf(dates[1]));
			for (Consumer<MonthDay> fn : listeners)
				fn.accept(monthDay);
		} catch (Exception e) {
			Log.error(this, "failed to generate month day time format with"
					+ "selected value " + value, e);
		}
	}

	void select(String value) {
		if (value == null)
			return;
		String[] dates = value.split("-");
		if (dates.length != 3)
			return;
		MonthDay monthDay = MonthDay.of(Integer.valueOf(dates[0]),
				Integer.valueOf(dates[1]));
		int monthIdx = monthDay.getMonthValue() - 1;
		monthCombo.select(monthIdx);
		dayCombo.select(monthDay.getDayOfMonth() - 1);
		int hourIdx = Integer.valueOf(dates[2]);
		hourCombo.select(hourIdx);
	}

	public String getSelection() {
		int month = monthCombo.getSelectionIndex() + 1;
		int day = dayCombo.getSelectionIndex() + 1;
		int hour = hourCombo.getSelectionIndex();
		return String.valueOf(month).concat("-").concat(String.valueOf(day))
				.concat("-").concat(String.valueOf(hour));
	}

	public void setEnabled(boolean enabled) {
		dayCombo.setEnabled(enabled);
		monthCombo.setEnabled(enabled);
		hourCombo.setEnabled(enabled);
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
