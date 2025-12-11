package sophena.rcp.editors.basedata.climate;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.M;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.UI;

public class ClimateDataEditor extends Editor {

	private final Logger log = LoggerFactory.getLogger(getClass());

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("climate.data",
				M.ClimateData);
		Editors.open(input, "sophena.ClimateDataEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new TablePage(this));
			addPage(new MapPage(this));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	private static class MapPage extends FormPage {

		public MapPage(ClimateDataEditor editor) {
			super(editor, "climate.MapPage", "Karte");
		}

		@Override
		protected void createFormContent(IManagedForm mform) {
			ScrolledForm form = UI.formHeader(mform, M.Location);
			FormToolkit tk = mform.getToolkit();
			Composite body = UI.formBody(form, tk);
			ClimateStationBrowser.create(body);
			form.reflow(true);
		}
	}
}
