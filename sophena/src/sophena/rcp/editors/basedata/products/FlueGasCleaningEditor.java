package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class FlueGasCleaningEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("flue.gas.cleaning.products",
				"Rauchgasreinigung");
		Editors.open(input, "sophena.products.FlueGasCleaningEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, ProductType.FLUE_GAS_CLEANING));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}