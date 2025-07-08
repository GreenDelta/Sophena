package sophena.rcp.editors.biogas.plant;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import sophena.model.Stats;
import sophena.model.biogas.SubstrateProfile;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class MonthPanel {

	private final SubstrateProfile profile;

	private final double[] values;
	private final Text[] texts;
	private final Text amountText;

	private MonthPanel(Composite parent, SubstrateProfile profile) {
		this.profile = profile;
		this.values = profile.monthlyPercentages != null
				? Stats.copy(profile.monthlyPercentages)
				: new double[]{
				8.5, 7.7, 8.5, 8.2, 8.5, 8.2, 8.5, 8.5, 8.2, 8.5, 8.2, 8.5};

		var g = new Group(parent, SWT.NONE);
		g.setText("Monatliche Verteilung [%]");
		UI.fillHorizontal(g);
		UI.gridLayout(g, 1);

		var top = new Composite(g, SWT.NONE);
		UI.fillHorizontal(top);
		UI.gridLayout(top, 3);
		amountText = UI.formText(top, "Jährliche Menge");
		Texts.on(amountText)
				.init(profile.annualAmount)
				.required()
				.decimal();
		UI.formLabel(top, "t/a");

		var sub = new Composite(g, SWT.NONE);
		UI.fillHorizontal(sub);
		UI.gridLayout(sub, 4);
		texts = new Text[]{
				cell(0, "Jan", sub), cell(6, "Jul", sub),
				cell(1, "Feb", sub), cell(7, "Aug", sub),
				cell(2, "Mär", sub), cell(8, "Sep", sub),
				cell(3, "Apr", sub), cell(9, "Okt", sub),
				cell(4, "Mai", sub), cell(10, "Nov", sub),
				cell(5, "Jun", sub), cell(11, "Dez", sub)
		};
	}

	static MonthPanel create(Composite parent, SubstrateProfile profile) {
		return new MonthPanel(parent, profile);
	}

	double[] values() {
		return values;
	}

	void setEnabled(boolean b) {
		amountText.setEnabled(b);
		for (var text : texts) {
			text.setEnabled(b);
		}
	}

	private Text cell(int index, String label, Composite parent) {
		var text = UI.formText(parent, label);
		Texts.on(text)
				.init(values[index])
				.decimal()
				.onChanged(s -> values[index] = Num.read(s));
		return text;
	}
}
