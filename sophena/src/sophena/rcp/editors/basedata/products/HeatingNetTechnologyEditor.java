package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class HeatingNetTechnologyEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput(
				"heating.net.construction.products", "Wärmenetz-Technik");
		Editors.open(input, "sophena.products.HeatingNetTechnologyEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new EditorPage(this, ProductType.HEATING_NET_TECHNOLOGY));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}