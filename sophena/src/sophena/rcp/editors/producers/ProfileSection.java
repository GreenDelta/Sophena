package sophena.rcp.editors.producers;


import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.io.ProducerProfiles;
import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.model.Stats;
import sophena.rcp.Icon;
import sophena.rcp.charts.ImageExport;
import sophena.rcp.charts.ProducerProfileChart;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class ProfileSection {

	private ProducerEditor editor;
	private ProducerProfileChart chart;
	private Text thermPowerText;

	private ProfileSection() {
	}

	static ProfileSection of(ProducerEditor editor) {
		ProfileSection section = new ProfileSection();
		section.editor = editor;
		return section;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	ProfileSection create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Erzeugerlastgang");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		chart = new ProducerProfileChart(comp, 250);
		if (producer().profile != null) {
			chart.setData(producer().profile);
		}
		createPowerTexts(comp, tk);
		Action imp = Actions.create(
				"Neuen Lastgang importieren",
				Icon.IMPORT_16.des(),
				this::importProfile);
		Actions.bind(section, imp, ImageExport.forXYGraph(
				"Erzeugerlastgang.jpg", () -> chart.graph));
		return this;
	}

	private void createPowerTexts(Composite parent, FormToolkit tk) {
		Producer p = producer();
		Composite c = tk.createComposite(parent);
		UI.gridLayout(c, 7);
		thermPowerText = UI.formText(c, tk, "Thermische Nennleistung");
		UI.gridData(thermPowerText, false, false).widthHint = 120;
		UI.formLabel(c, "kW");
		Texts.on(thermPowerText)
				.init(Num.intStr(p.profileMaxPower))
				.required().decimal()
				.onChanged(s -> {
					producer().profileMaxPower = Texts
							.getDouble(thermPowerText);
					editor.setDirty();
				});
		if (!Producers.isCoGenPlant(p))
			return;
		UI.gridData(UI.filler(c), false, false).widthHint = 25;
		Text electT = UI.formText(c, tk, "Elektrische Nennleistung");
		UI.gridData(electT, false, false).widthHint = 120;
		UI.formLabel(c, "kW");
		Texts.on(electT)
				.init(Num.intStr(p.profileMaxPowerElectric))
				.required().decimal()
				.onChanged(s -> {
					producer().profileMaxPowerElectric = Texts
							.getDouble(electT);
					editor.setDirty();
				});
	}

	void importProfile() {
		var file = FileChooser.open("*.csv", "*.txt");
		if (file == null)
			return;
		try {
			var r = ProducerProfiles.read(file);
			if (r.isError()) {
				MsgBox.error(r.message().orElse(
						"Datei konnte nicht gelesen werden"));
				return;
			}
			if (r.isWarning()) {
				MsgBox.warn(r.message().orElse(
						"Die Datei enthält Formatfehler"));
			}

			Producer p = producer();
			p.profile = r.get();
			p.profileMaxPower = Stats.max(p.profile.maxPower);
			if (thermPowerText != null) {
				thermPowerText.setText(Num.intStr(p.profileMaxPower));
			}
			chart.setData(p.profile);
			editor.setDirty();
		} catch (Exception e) {
			MsgBox.error("Datei konnte nicht gelesen werden",
					e.getMessage());
			Log.error(this, "Failed to read producer profile " + file, e);
		}
	}

}
