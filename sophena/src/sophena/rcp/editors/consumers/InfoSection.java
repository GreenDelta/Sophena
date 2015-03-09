package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.Dao;
import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.EntityCombo;
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
		createTypeCombo(composite, toolkit);
		createStateCombo(composite, toolkit);
		if(consumer().isDemandBased()) {
			createLoadText(composite, toolkit);
		}
	}

	private void createNameText(FormToolkit toolkit, Composite composite) {
		Text nt = UI.formText(composite, toolkit, M.Name);
		if (consumer().getName() != null)
			nt.setText(consumer().getName());
		nt.addModifyListener((e) -> {
			consumer().setName(nt.getText());
			editor.setDirty();
		});
	}

	private void createDescriptionText(FormToolkit toolkit, Composite composite) {
		Text dt = UI.formMultiText(composite, toolkit, M.Description);
		if (consumer().getDescription() != null)
			dt.setText(consumer().getDescription());
		dt.addModifyListener((e) -> {
			consumer().setDescription(dt.getText());
			editor.setDirty();
		});
	}

	private void createTypeCombo(Composite composite, FormToolkit toolkit) {
		EntityCombo<BuildingType> combo = new EntityCombo<>();
		combo.create(M.BuildingType, composite, toolkit);
		Dao<BuildingType> dao = new Dao<>(BuildingType.class, App.getDb());
		combo.setInput(dao.getAll());
		combo.select(consumer().getBuildingType());
		combo.onSelect((t) -> {
			consumer().setBuildingType(t);
			editor.setDirty();
		});
	}

	private void createStateCombo(Composite composite, FormToolkit toolkit) {
		EntityCombo<BuildingState> combo = new EntityCombo<>();
		combo.create(M.BuildingState, composite, toolkit);
		Dao<BuildingState> dao = new Dao<>(BuildingState.class, App.getDb());
		combo.setInput(dao.getAll());
		combo.select(consumer().getBuildingState());
		combo.onSelect((s) -> {
			consumer().setBuildingState(s);
			editor.setDirty();
		});
	}

	private void createLoadText(Composite composite, FormToolkit toolkit) {
		Text t = UI.formText(composite, toolkit, M.HeatingLoad);
		t.setText(Numbers.toString(consumer().getHeatingLoad()));
		t.addModifyListener((e) -> consumer().setHeatingLoad(
				Numbers.read(t.getText())));
	}
}
