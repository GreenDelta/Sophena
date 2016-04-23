package sophena.rcp.editors.consumers;

import java.util.List;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.BuildingStateDao;
import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class InfoSection {

	private ConsumerEditor editor;
	private HeatDemandSection demandSection;
	private EntityCombo<BuildingState> stateCombo;

	private InfoSection() {
	}

	static InfoSection of(ConsumerEditor editor) {
		InfoSection section = new InfoSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.consumer;
	}

	void setDemandSection(HeatDemandSection demandSection) {
		this.demandSection = demandSection;
	}

	InfoSection create(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, M.ConsumerInformation);
		UI.gridLayout(comp, 3);
		nameText(tk, comp);
		descriptionText(tk, comp);
		buildingTypeCombo(comp, tk);
		buildingStateCombo(comp, tk);
		floorSpaceText(comp, tk);
		return this;
	}

	private void nameText(FormToolkit tk, Composite comp) {
		Text nt = UI.formText(comp, tk, M.Name);
		Texts.set(nt, consumer().name);
		Texts.on(nt).required().onChanged((t) -> {
			consumer().name = t;
			editor.setDirty();
		});
		UI.filler(comp, tk);
	}

	private void descriptionText(FormToolkit tk, Composite comp) {
		Text dt = UI.formMultiText(comp, tk, M.Description);
		Texts.set(dt, consumer().description);
		dt.addModifyListener((e) -> {
			consumer().description = dt.getText();
			editor.setDirty();
		});
		UI.filler(comp, tk);
	}

	private void buildingTypeCombo(Composite comp, FormToolkit tk) {
		Combo combo = UI.formCombo(comp, M.BuildingType);
		BuildingState state = consumer().buildingState;
		BuildingType[] types = BuildingType.values();
		String[] items = new String[types.length];
		int selected = 0;
		for (int i = 0; i < types.length; i++) {
			items[i] = Labels.get(types[i]);
			if (state != null && state.type == types[i])
				selected = i;
		}
		combo.setItems(items);
		combo.select(selected);
		Controls.onSelect(combo, e -> {
			BuildingType type = types[combo.getSelectionIndex()];
			updateSateCombo(type);
		});
		UI.filler(comp, tk);
	}

	private void updateSateCombo(BuildingType type) {
		if (type == null || stateCombo == null)
			return;
		BuildingState current = consumer().buildingState;
		if (current != null && current.type == type)
			return;
		BuildingStateDao dao = new BuildingStateDao(App.getDb());
		List<BuildingState> states = dao.getAllWith(type);
		BuildingState state = BuildingStateDao.getDefault(states);
		if (state != null) {
			stateCombo.setInput(states);
			stateCombo.select(state);
		}
	}

	private void buildingStateCombo(Composite comp, FormToolkit tk) {
		stateCombo = new EntityCombo<>();
		stateCombo.create(M.BuildingState, comp, tk);
		BuildingState state = consumer().buildingState;
		if (state != null && state.type != null) {
			BuildingStateDao dao = new BuildingStateDao(App.getDb());
			stateCombo.setInput(dao.getAllWith(state.type));
			stateCombo.select(state);
		}
		stateCombo.onSelect(s -> {
			consumer().buildingState = s;
			editor.setDirty();
			if (demandSection != null) {
				demandSection.updateBuildingState(s);
			}
		});
		UI.filler(comp, tk);
	}

	private void floorSpaceText(Composite comp, FormToolkit tk) {
		Text t = UI.formText(comp, tk, "Wohnfläche");
		if (consumer().floorSpace != 0)
			Texts.set(t, consumer().floorSpace);
		Texts.on(t).decimal().onChanged(text -> {
			consumer().floorSpace = Num.read(text);
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "m2");
	}

}
