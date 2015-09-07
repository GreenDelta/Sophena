package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class PlanningEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("planning.products",
				"Planung");
		Editors.open(input, "sophena.products.PlanningEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, ProductType.PLANNING));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}
