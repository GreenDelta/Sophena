package sophena.rcp.editors.basedata.boilers;

import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class CoGenPlantEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.cogen_plants",
				"KWK-Anlagen");
		Editors.open(input, "sophena.CoGenPlantEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, true));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}
