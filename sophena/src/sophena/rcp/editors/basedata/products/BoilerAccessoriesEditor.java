package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class BoilerAccessoriesEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("boiler.accessories.products",
				"Kesselzubehör");
		Editors.open(input, "sophena.products.BoilerAccessoriesEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, ProductType.BOILER_ACCESSORIES));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}
}