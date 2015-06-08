package sophena.rcp.editors.heatnets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MonthDayBox;
import sophena.rcp.utils.UI;

class InterruptionSection {

	private HeatNetEditor editor;

	private MonthDayBox startBox;
	private MonthDayBox endBox;

	InterruptionSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	void create(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				"Wärmenetz - Unterbrechung");
		UI.gridLayout(composite, 2).horizontalSpacing = 25;
		createCheck(toolkit, composite);
		startBox = new MonthDayBox(M.Start, composite, toolkit);
		startBox.onSelect((monthDay) -> {
			System.out.println(monthDay);
		});

		endBox = new MonthDayBox(M.End, composite, toolkit);


		endBox.onSelect((monthDay) -> {
			System.out.println(monthDay);
		});
	}

	private void createCheck(FormToolkit toolkit, Composite composite) {
		Button check = new Button(composite, SWT.CHECK);
		check.setText("Mit Unterbrechung");
		Controls.onSelect(check, (e) -> {
			boolean enabled = check.getSelection();
			startBox.setEnabled(enabled);
			endBox.setEnabled(enabled);
		});
		UI.formLabel(composite, toolkit, "");
	}

}
