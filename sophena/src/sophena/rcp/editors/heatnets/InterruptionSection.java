package sophena.rcp.editors.heatnets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.model.HeatNet;
import sophena.model.TimeInterval;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MonthDayBox;
import sophena.rcp.utils.UI;

import java.time.MonthDay;
import java.util.UUID;

class InterruptionSection {

	private final HeatNetEditor editor;

	private MonthDayBox startBox;
	private MonthDayBox endBox;
	private LoadCurveSection loadCurve;

	InterruptionSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	public void setLoadCurve(LoadCurveSection loadCurve) {
		this.loadCurve = loadCurve;
	}

	private HeatNet heatNet() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				"Wärmenetz - Unterbrechung");
		UI.gridLayout(composite, 2).horizontalSpacing = 25;
		createCheck(toolkit, composite);
		createStartBox(toolkit, composite);
		createEndBox(toolkit, composite);
	}

	private void createCheck(FormToolkit tk, Composite comp) {
		var check = tk.createButton(comp, "Mit Unterbrechung", SWT.CHECK);
		check.setSelection(heatNet().interruption != null);
		Controls.onSelect(check, (e) -> {
			boolean enabled = check.getSelection();
			startBox.setEnabled(enabled);
			endBox.setEnabled(enabled);
			if (enabled) {
				initInterruptionValues();
			} else {
				heatNet().interruption = null;
			}
			updateLoadCurve();
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "");
	}

	private void initInterruptionValues() {
		TimeInterval interruption = heatNet().interruption;
		if (interruption == null) {
			interruption = new TimeInterval();
			interruption.id = UUID.randomUUID().toString();
			heatNet().interruption = interruption;
		}
		if (interruption.start == null) {
			MonthDay start = startBox.getSelection();
			if (start != null)
				interruption.start = start.toString();
		}
		if (interruption.end == null) {
			MonthDay end = endBox.getSelection();
			if (end != null)
				interruption.end = end.toString();
		}
	}

	private void createStartBox(FormToolkit tk, Composite comp) {
		startBox = new MonthDayBox(M.Start, comp, tk);
		TimeInterval interruption = heatNet().interruption;
		startBox.setEnabled(interruption != null);
		if (interruption != null) {
			initBoxValue(startBox, interruption.start);
		}
		startBox.onSelect((monthDay) -> {
			TimeInterval i = heatNet().interruption;
			if (i == null || monthDay == null)
				return;
			i.start = monthDay.toString();
			updateLoadCurve();
			editor.setDirty();
		});
	}

	private void createEndBox(FormToolkit toolkit, Composite composite) {
		endBox = new MonthDayBox(M.End, composite, toolkit);
		TimeInterval interruption = heatNet().interruption;
		endBox.setEnabled(interruption != null);
		if (interruption != null) {
			initBoxValue(endBox, interruption.end);
		}
		endBox.onSelect((monthDay) -> {
			TimeInterval i = heatNet().interruption;
			if (i == null || monthDay == null)
				return;
			i.end = monthDay.toString();
			updateLoadCurve();
			editor.setDirty();
		});
	}

	private void initBoxValue(MonthDayBox box, String monthDay) {
		if (box == null || monthDay == null)
			return;
		try {
			MonthDay value = MonthDay.parse(monthDay);
			box.select(value);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to parse MonthDay " + monthDay, e);
		}
	}

	private void updateLoadCurve() {
		if (loadCurve == null)
			return;
		loadCurve.setData(NetLoadProfile.get(heatNet()));
	}

}
