package sophena.rcp.editors.heatnets;

import java.time.MonthDay;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.ProjectLoad;
import sophena.model.HeatNet;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MonthDayBox;
import sophena.rcp.utils.UI;

class InterruptionSection {

	private HeatNetEditor editor;

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

	private void createCheck(FormToolkit toolkit, Composite composite) {
		Button check = new Button(composite, SWT.CHECK);
		check.setText("Mit Unterbrechung");
		check.setSelection(heatNet().withInterruption);
		Controls.onSelect(check, (e) -> {
			boolean enabled = check.getSelection();
			heatNet().withInterruption = enabled;
			startBox.setEnabled(enabled);
			endBox.setEnabled(enabled);
			if (enabled)
				initInterruptionValues();
			updateLoadCurve();
			editor.setDirty();
		});
		UI.formLabel(composite, toolkit, "");
	}

	private void initInterruptionValues() {
		HeatNet net = heatNet();
		if (net.interruptionStart == null) {
			MonthDay start = startBox.getSelection();
			if (start != null)
				net.interruptionStart = start.toString();
		}
		if (net.interruptionEnd == null) {
			MonthDay end = endBox.getSelection();
			if (end != null)
				net.interruptionEnd = end.toString();
		}
	}

	private void createStartBox(FormToolkit toolkit, Composite composite) {
		startBox = new MonthDayBox(M.Start, composite, toolkit);
		startBox.setEnabled(heatNet().withInterruption);
		initBoxValue(startBox, heatNet().interruptionStart);
		startBox.onSelect((monthDay) -> {
			if (monthDay == null)
				return;
			heatNet().interruptionStart = monthDay.toString();
			updateLoadCurve();
			editor.setDirty();
		});
	}

	private void createEndBox(FormToolkit toolkit, Composite composite) {
		endBox = new MonthDayBox(M.End, composite, toolkit);
		endBox.setEnabled(heatNet().withInterruption);
		initBoxValue(endBox, heatNet().interruptionEnd);
		endBox.onSelect((monthDay) -> {
			if (monthDay == null)
				return;
			heatNet().interruptionEnd = monthDay.toString();
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
		double[] curve = ProjectLoad.getNetLoadCurve(heatNet());
		loadCurve.setData(curve);
	}

}
