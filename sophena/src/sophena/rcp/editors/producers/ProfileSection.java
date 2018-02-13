package sophena.rcp.editors.producers;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.Producer;
import sophena.model.ProducerProfile;
import sophena.rcp.Icon;
import sophena.rcp.charts.ProducerProfileChart;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.UI;

public class ProfileSection {

	private ProducerEditor editor;
	private ProducerProfileChart chart;

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

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Erzeugerlastgang");
		UI.gridData(section, true, false);
		Composite comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		chart = new ProducerProfileChart(comp, 250);
		if (producer().profile != null)
			chart.setData(producer().profile);
		Action imp = Actions.create("Importiere", Icon.IMPORT_16.des(), () -> {
			File f = FileChooser.open("*.csv", ".txt");
			if (f == null)
				return;
			try {
				producer().profile = ProducerProfile.read(f);
				chart.setData(producer().profile);
				editor.setDirty();
			} catch (Exception e) {
				MsgBox.error("Datei konnte nicht gelesen werden", e.getMessage());
				Log.error(this, "Failed to read producer profile " + f, e);
			}
		});
		Actions.bind(section, imp);
	}

}
