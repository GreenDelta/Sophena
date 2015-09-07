package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class HeatingNetConstructionEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput(
				"heating.net.construction.products", "Wärmenetz-Bau");
		Editors.open(input, "sophena.products.HeatingNetConstructionEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, ProductType.HEATING_NET_CONSTRUCTION));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}