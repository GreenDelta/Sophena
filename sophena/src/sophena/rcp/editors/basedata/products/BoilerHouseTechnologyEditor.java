package sophena.rcp.editors.basedata.products;

import sophena.model.ProductType;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

@Deprecated
public class BoilerHouseTechnologyEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput(
				"boiler.house.technology.products", "Heizhaus-Technik");
		Editors.open(input, "sophena.products.BoilerHouseTechnologyEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new ProductPage(this, ProductType.BOILER_HOUSE_TECHNOLOGY));
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

}