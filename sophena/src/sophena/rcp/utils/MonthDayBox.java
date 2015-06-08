package sophena.rcp.utils;

import java.time.MonthDay;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class MonthDayBox {

	private final String[] MONTHS = {
			"Januar", "Februar", "März", "April", "Mai", "Juni", "Juli",
			"August", "September", "Oktober", "November", "Dezember" };
	private final int[] DAYS_IN_MONTH = {
			31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };

	private List<Consumer<MonthDay>> listeners = new ArrayList<>();
	private Combo dayCombo;
	private Combo monthCombo;

	public MonthDayBox(String label, Composite parent, FormToolkit tk) {
		Composite c = tk.createComposite(parent);
		UI.innerGrid(c, 3);
		tk.createLabel(c, label);
		dayCombo = createCombo(c, 25, getDayItems(0));
		tk.adapt(dayCombo);
		monthCombo = createCombo(c, 75, MONTHS);
		tk.adapt(monthCombo);
		Controls.onSelect(monthCombo, (e) -> monthChanged());
		Controls.onSelect(dayCombo, (e) -> fireChange());
	}

	public void onSelect(Consumer<MonthDay> fn) {
		if (fn == null)
			return;
		listeners.add(fn);
	}

	public void select(MonthDay value) {
		if (value == null)
			return;
		int monthIdx = value.getMonthValue() - 1;
		monthCombo.select(monthIdx);
		dayCombo.setItems(getDayItems(monthIdx));
		int dayIdx = value.getDayOfMonth() - 1;
		dayCombo.select(dayIdx);
	}

	public void setEnabled(boolean enabled) {
		dayCombo.setEnabled(enabled);
		monthCombo.setEnabled(enabled);
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
		int month = monthCombo.getSelectionIndex() + 1;
		int day = dayCombo.getSelectionIndex() + 1;
		MonthDay value = MonthDay.of(month, day);
		for (Consumer<MonthDay> fn : listeners)
			fn.accept(value);
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
		int count = DAYS_IN_MONTH[monthIndex];
		String[] items = new String[count];
		for (int i = 0; i < count; i++)
			items[i] = Integer.toString(i + 1).concat(".");
		return items;
	}
}
