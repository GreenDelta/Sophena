package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.BuildingState;
import sophena.model.Consumer;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class InfoSection {

	private ConsumerEditor editor;

	private InfoSection() {
	}

	static InfoSection of(ConsumerEditor editor) {
		InfoSection section = new InfoSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.getConsumer();
	}

	void create(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				M.ConsumerInformation);
		createNameText(toolkit, composite);
		createDescriptionText(toolkit, composite);
		createStateFields(composite, toolkit);
		Text loadHoursText = UI.formText(composite, toolkit, "Volllaststunden");
		Texts.on(loadHoursText)
				.init(consumer().loadHours)
				.readOnly();
	}

	private void createNameText(FormToolkit toolkit, Composite composite) {
		Text nt = UI.formText(composite, toolkit, M.Name);
		Texts.set(nt, consumer().name);
		Texts.on(nt).required().onChanged((t) -> {
			consumer().name = t;
			editor.setDirty();
		});
	}

	private void createDescriptionText(FormToolkit toolkit,
			Composite composite) {
		Text dt = UI.formMultiText(composite, toolkit, M.Description);
		Texts.set(dt, consumer().description);
		dt.addModifyListener((e) -> {
			consumer().description = dt.getText();
			editor.setDirty();
		});
	}

	private void createStateFields(Composite composite, FormToolkit toolkit) {
		BuildingState state = consumer().buildingState;
		if (state == null)
			return;
		Text tt = UI.formText(composite, toolkit, M.BuildingType);
		Texts.on(tt).readOnly().init(Labels.get(state.type));
		Text st = UI.formText(composite, toolkit, M.BuildingState);
		Texts.on(st).readOnly().init(state.name);
	}
}
