package sophena.rcp.editors.basedata.boilers;

import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class BoilerEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.boilers", "Heizkessel");
		Editors.open(input, "sophena.BoilerEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, false));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}
