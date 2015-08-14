package sophena.rcp.editors.projects;

import java.util.Collections;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.db.daos.WeatherStationDao;
import sophena.model.Project;
import sophena.model.WeatherStation;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private ProjectEditor editor;

	public InfoPage(ProjectEditor editor) {
		super(editor, "sophena.ProjectInfoPage", "Projektinformationen");
		this.editor = editor;
	}

	private Project project() {
		return editor.getProject();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, editor.getProject().name);
		FormToolkit toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createInfoSection(body, toolkit);
		form.reflow(true);
	}

	private void createInfoSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit,
				M.ProjectInformation);
		createNameText(toolkit, composite);
		createDescriptionText(toolkit, composite);
		createDurationText(toolkit, composite);
		createStationCombo(toolkit, composite);
	}

	private void createNameText(FormToolkit toolkit, Composite composite) {
		Text t = UI.formText(composite, toolkit, M.Name);
		Texts.on(t)
				.init(project().name)
				.required()
				.onChanged((s) -> {
					project().name = t.getText();
					editor.setDirty();
				});
	}

	private void createDescriptionText(FormToolkit toolkit,
			Composite composite) {
		Text t = UI.formMultiText(composite, toolkit, M.Description);
		Texts.on(t)
				.init(project().description)
				.onChanged((s) -> {
					project().description = t.getText();
					editor.setDirty();
				});
	}

	private void createDurationText(FormToolkit toolkit, Composite composite) {
		Text t = UI.formText(composite, toolkit, M.ProjectDurationYears);
		Texts.on(t)
				.init(project().getProjectDuration())
				.required()
				.integer()
				.onChanged((s) -> {
					project().setProjectDuration(Texts.getInt(t));
					editor.setDirty();
				});
	}

	private void createStationCombo(FormToolkit toolkit, Composite composite) {
		EntityCombo<WeatherStationDescriptor> combo = new EntityCombo<>();
		combo.create("Wetterstation", composite, toolkit);
		WeatherStationDao dao = new WeatherStationDao(App.getDb());
		List<WeatherStationDescriptor> list = dao.getDescriptors();
		Collections.sort(list,
				(w1, w2) -> Strings.compare(w1.name, w2.name));
		combo.setInput(list);
		WeatherStation s = project().getWeatherStation();
		if (s != null)
			combo.select(s.toDescriptor());
		combo.onSelect((d) -> {
			if (d == null) {
				return;
			}
			WeatherStation selected = dao.get(d.id);
			project().setWeatherStation(selected);
			editor.setDirty();
		});
	}
}
