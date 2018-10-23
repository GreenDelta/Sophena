package sophena.rcp.editors.producers;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.Producer;
import sophena.model.ProducerProfile;
import sophena.model.ProductType;
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
		if (producer().profile != null) {
			chart.setData(producer().profile);
		}
		createPowerTexts(comp, tk);
		Action imp = importAction();
		Actions.bind(section, imp, ImageExport.forXYGraph(
				"Erzeugerlastgang.jpg", () -> chart.graph));
	}

	private void createPowerTexts(Composite parent, FormToolkit tk) {
		Producer p = producer();
		Composite c = tk.createComposite(parent);
		UI.innerGrid(c, 7).horizontalSpacing = 10;
		Text thermT = UI.formText(c, tk, "Thermische Nennleistung");
		UI.gridData(thermT, false, false).widthHint = 80;
		UI.formLabel(c, "kW");
		Texts.on(thermT).init(Num.intStr(p.profileMaxPower))
				.required().decimal().onChanged(s -> {
					producer().profileMaxPower = Texts.getDouble(thermT);
					editor.setDirty();
				});
		if (p.productGroup == null
				|| p.productGroup.type != ProductType.COGENERATION_PLANT)
			return;
		UI.gridData(UI.filler(c), false, false).widthHint = 40;
		Text electT = UI.formText(c, tk, "Elektrische Nennleistung");
		UI.gridData(electT, false, false).widthHint = 80;
		UI.formLabel(c, "kW");
		Texts.on(electT).init(Num.intStr(p.profileMaxPowerElectric))
				.required().decimal().onChanged(s -> {
					producer().profileMaxPowerElectric = Texts
							.getDouble(electT);
					editor.setDirty();
				});
	}

	private Action importAction() {
		return Actions.create("Importiere", Icon.IMPORT_16.des(), () -> {
			File f = FileChooser.open("*.csv", ".txt");
			if (f == null)
				return;
			try {
				producer().profile = ProducerProfile.read(f);
				chart.setData(producer().profile);
				editor.setDirty();
			} catch (Exception e) {
				MsgBox.error("Datei konnte nicht gelesen werden",
						e.getMessage());
				Log.error(this, "Failed to read producer profile " + f, e);
			}
		});
	}

}
