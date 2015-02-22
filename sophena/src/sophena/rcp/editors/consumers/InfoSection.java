package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.daos.Dao;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.UI;

class InfoSection {

	private ConsumerEditor editor;

	private InfoSection(){
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
		Composite composite = UI.formSection(body, toolkit, M.ConsumerInformation);
		Text nt = UI.formText(composite, toolkit, M.Name);
		nt.addModifyListener((e) -> consumer().setName(nt.getText()));
		Text dt = UI.formMultiText(composite, toolkit, M.Description);
		dt.addModifyListener((e) -> consumer().setDescription(dt.getText()));
		createTypeCombo(composite, toolkit);
		UI.formCombo(composite, toolkit, M.BuildingState);
	}

	private void createTypeCombo(Composite composite, FormToolkit toolkit) {
		EntityCombo<BuildingType> combo = new EntityCombo<>(M.BuildingType);
		combo.create(composite, toolkit);
		try {
			Dao<BuildingType> dao = new Dao<>(BuildingType.class, App.getDb());
			combo.setInput(dao.getAll());
			combo.select(consumer().getBuildingType());
			combo.onSelect((t) -> {
				consumer().setBuildingType(t);
				editor.setDirty();
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to load building types", e);
		}
	}
}
