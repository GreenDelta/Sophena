package sophena.rcp.editors.projects;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.db.daos.WeatherStationDao;
import sophena.model.Project;
import sophena.model.descriptors.WeatherStationDescriptor;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.app.Workspace;
import sophena.rcp.colors.Colors;
import sophena.rcp.editors.CostSettingsPanel;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Desktop;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private final ProjectEditor editor;

	public InfoPage(ProjectEditor editor) {
		super(editor, "sophena.ProjectInfoPage", "Projektinformationen");
		this.editor = editor;
	}

	private Project project() {
		return editor.project;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var form = UI.formHeader(mform, project().name);
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		createInfoSection(body, tk);
		CostSettingsPanel panel = new CostSettingsPanel(
			editor, () -> project().costSettings, () -> form.reflow(true));
		panel.isForProject = true;
		panel.render(tk, body);
		form.reflow(true);
	}

	private void createInfoSection(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, M.Project);
		createNameText(tk, comp);
		createDescriptionText(tk, comp);
		createDurationText(tk, comp);
		createStationCombo(tk, comp);
		UI.formLabel(comp, tk, "Datenbankpfad");
		File dbDir = Workspace.dir();
		var link = tk.createHyperlink(comp, dbDir.getAbsolutePath(), SWT.NONE);
		link.setForeground(Colors.getLinkBlue());
		Controls.onClick(link, _ -> Desktop.browse(dbDir.toURI().toASCIIString()));
	}

	private void createNameText(FormToolkit toolkit, Composite composite) {
		Text t = UI.formText(composite, toolkit, M.Name);
		Texts.on(t)
			.init(project().name)
			.required()
			.onChanged(_ -> {
				project().name = t.getText();
				editor.setDirty();
			});
	}

	private void createDescriptionText(FormToolkit tk, Composite comp) {
		var t = UI.formMultiText(comp, tk, M.Description);
		Texts.on(t)
			.init(project().description)
			.onChanged(_ -> {
				project().description = t.getText();
				editor.setDirty();
			});
	}

	private void createDurationText(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, tk, M.ProjectDurationYears);
		Texts.on(t)
			.init(project().duration)
			.required()
			.integer()
			.onChanged(_ -> {
				project().duration = Texts.getInt(t);
				editor.setDirty();
			});
	}

	private void createStationCombo(FormToolkit tk, Composite comp) {
		var combo = new EntityCombo<WeatherStationDescriptor>();
		combo.create("Wetterstation", comp, tk);
		var dao = new WeatherStationDao(App.getDb());
		var list = dao.getDescriptors();
		Sorters.byName(list);
		combo.setInput(list);
		var s = project().weatherStation;
		if (s != null) {
			combo.select(s.toDescriptor());
		}
		combo.onSelect(d -> {
			if (d == null) {
				return;
			}
			project().weatherStation = dao.get(d.id);
			editor.setDirty();
		});
	}
}
