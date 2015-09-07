package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class BuildingProductEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("building.products",
				"Gebäude");
		Editors.open(input, "sophena.products.BuildingProductEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, ProductType.BUILDING));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}